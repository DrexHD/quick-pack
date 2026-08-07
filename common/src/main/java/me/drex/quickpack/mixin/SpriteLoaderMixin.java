package me.drex.quickpack.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.quickpack.config.ConfigManager;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {

    @Unique
    private Executor quick_pack$mipmapExecutor = null;

    @WrapOperation(
        method = "stitch",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;runAsync(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
        )
    )
    private CompletableFuture<Void> skipSerialMipmapGeneration(Runnable task, Executor executor, Operation<CompletableFuture<Void>> original) {
        if (!ConfigManager.config.parallelMipmapGeneration) {
            return original.call(task, executor);
        }
        this.quick_pack$mipmapExecutor = executor;
        return CompletableFuture.completedFuture(null);
    }

    @ModifyReturnValue(method = "stitch", at = @At("RETURN"))
    private SpriteLoader.Preparations parallelMipmapGeneration(SpriteLoader.Preparations preparations) {
        Executor executor = this.quick_pack$mipmapExecutor;
        if (executor == null) {
            return preparations;
        }
        this.quick_pack$mipmapExecutor = null;

        Collection<TextureAtlasSprite> sprites = preparations.regions().values();
        int mipLevel = preparations.mipLevel();
        CompletableFuture<Void> readyForUpload = CompletableFuture.allOf(
            sprites.stream()
                .map(sprite -> CompletableFuture.runAsync(() -> sprite.contents().increaseMipLevel(mipLevel), executor))
                .toArray(CompletableFuture[]::new)
        );

        return new SpriteLoader.Preparations(
            preparations.width(),
            preparations.height(),
            mipLevel,
            preparations.missing(),
            preparations.regions(),
            readyForUpload
        );
    }
}

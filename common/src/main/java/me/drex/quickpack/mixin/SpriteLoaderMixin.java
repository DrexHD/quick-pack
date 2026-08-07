package me.drex.quickpack.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.drex.quickpack.config.ConfigManager;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {

    @WrapOperation(
        method = "stitch",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;runAsync(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
        )
    )
    private CompletableFuture<Void> parallelMipmapGeneration(
        Runnable task,
        Executor executor,
        Operation<CompletableFuture<Void>> original,
        @Local Map<?, TextureAtlasSprite> regions,
        @Local(ordinal = 6) int mipLevel
    ) {
        if (!ConfigManager.config.parallelMipmapGeneration) {
            return original.call(task, executor);
        }
        return CompletableFuture.allOf(
            regions.values().stream()
                .map(sprite -> CompletableFuture.runAsync(() -> sprite.contents().increaseMipLevel(mipLevel), executor))
                .toArray(CompletableFuture[]::new)
        );
    }
}

package me.drex.quickpack.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.quickpack.config.ConfigManager;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.function.Consumer;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {
    @WrapOperation(
        //? if >= 26.1 {
        method = "lambda$stitch$2",
        //? } else {
        /*method = {
            "method_45840", // fabric
            "lambda$stitch$2", // forge
            "lambda$stitch$3" // neoforge
        },
        *///? }
        require = 1,
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V"
        )
    )
    private static void parallelMipmapGeneration(
        Collection<TextureAtlasSprite> instance, Consumer<? super TextureAtlasSprite> consumer, Operation<Void> original
    ) {
        if (!ConfigManager.config.parallelMipmapGeneration) {
            original.call(instance, consumer);
        } else {
            instance.parallelStream().forEach(consumer);
        }
    }
}

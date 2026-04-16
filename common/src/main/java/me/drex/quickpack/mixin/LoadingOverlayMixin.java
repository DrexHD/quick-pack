package me.drex.quickpack.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.quickpack.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(
        method = "render",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;fadeIn:Z",
            ordinal = 2,
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean removeFadeOut(final LoadingOverlay instance, Operation<Boolean> original) {
        if (ConfigManager.config.removeLoadingOverlayFadeOut) {
            this.minecraft.setOverlay(null);
            return true;
        }
        return original.call(instance);
    }
}

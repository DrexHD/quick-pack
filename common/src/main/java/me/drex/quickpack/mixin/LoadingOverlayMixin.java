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

    //? if >= 1.21.11 {
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;isReadyToFadeOut()Z"
        )
    )
    //? } else {
    /*@WrapOperation(
        method = "render",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;fadeIn:Z",
            ordinal = 2,
            opcode = Opcodes.GETFIELD
        )
    )
    *///? }
    private boolean removeFadeOut(LoadingOverlay instance, Operation<Boolean> original) {
        if (ConfigManager.config.removeLoadingOverlayFadeOut) {
            //? if >= 26.2 {
            this.minecraft.gui.setOverlay(null);
            //? } else {
            /*this.minecraft.setOverlay(null);
            *///? }
            //? if >= 1.21.11 {
            return true;
            //? } else {
            /*return false;
            *///? }
        }
        return original.call(instance);
    }
}

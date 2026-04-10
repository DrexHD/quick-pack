package me.drex.quickpack.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;isReadyToFadeOut()Z"
        )
    )
    private boolean removeFadeOut(final LoadingOverlay instance) {
        this.minecraft.setOverlay(null);
        return true;
    }
}

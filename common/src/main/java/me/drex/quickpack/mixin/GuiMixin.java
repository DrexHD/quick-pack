package me.drex.quickpack.mixin;

import me.drex.quickpack.config.ConfigManager;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyArg(
        method = "lambda$buildInitialScreens$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/TitleScreen;<init>(ZLnet/minecraft/client/gui/components/LogoRenderer;)V"
        ),
        index = 0
    )
    public boolean disableFadeIn(boolean fading) {
        if (ConfigManager.config.removeLoadingOverlayFadeOut) {
            return false;
        }
        return fading;
    }

}

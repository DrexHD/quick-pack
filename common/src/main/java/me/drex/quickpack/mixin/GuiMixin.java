package me.drex.quickpack.mixin;

import me.drex.quickpack.config.ConfigManager;
//? if >= 26.2 {
import net.minecraft.client.gui.Gui;
//? } else {
/*import net.minecraft.client.Minecraft;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//? if >= 26.2 {
@Mixin(Gui.class)
//? } else {
/*@Mixin(Minecraft.class)
*///? }
public abstract class GuiMixin {
    @ModifyArg(
        //? if >= 26.1.2 {
        method = "lambda$buildInitialScreens$0",
        //? } else if >= 1.21.1 {
        /*method = {
            "lambda$buildInitialScreens$9", // forge
            "lambda$buildInitialScreens$8", // neoforge
            "method_53528", // fabric
        },
        *///? } else {
        /*method = "setInitialScreen",
        *///? }
        at = @At(
            value = "INVOKE",
            //? if >= 1.21.11 {
            target = "Lnet/minecraft/client/gui/screens/TitleScreen;<init>(ZLnet/minecraft/client/gui/components/LogoRenderer;)V"
            //? } else {
            /*target = "Lnet/minecraft/client/gui/screens/TitleScreen;<init>(Z)V"
            *///? }
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

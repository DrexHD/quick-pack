package me.drex.quickpack.mixin;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(FontManager.class)
public abstract class FontManagerMixin {
    /**
     * Vanilla iterates every supported codepoint and probes all providers (in reverse) until one has a glyph.
     * Instead, iterate each provider's own glyphs once and skip codepoints already claimed by a later provider.
     */
    @WrapOperation(
        method = "finalizeProviderLoading",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/ints/IntSet;forEach(Lit/unimi/dsi/fastutil/ints/IntConsumer;)V"
        )
    )
    private void fastFinalizeProviderLoading(
        IntSet supportedGlyphs,
        IntConsumer consumer,
        Operation<Void> original,
        @Local(argsOnly = true) List<GlyphProvider.Conditional> providers
    ) {
        IntSet claimedGlyphs = new IntOpenHashSet();

        for (int providerIndex = providers.size() - 1; providerIndex >= 0; providerIndex--) {
            GlyphProvider provider = providers.get(providerIndex).provider();

            for (int codepoint : provider.getSupportedGlyphs()) {
                if (codepoint == 32 || claimedGlyphs.contains(codepoint)) continue;

                if (provider.getGlyph(codepoint) != null) {
                    claimedGlyphs.add(codepoint);
                }
            }
        }
    }
}
//? } else {
/*import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class FontManagerMixin {
}
*///? }

package me.drex.quickpack.mixin;

//? if >= 1.21.1 {
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SpaceProvider;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(FontManager.class)
public abstract class FontManagerMixin {
    /**
     * @author DrexHD
     * @reason Optimize font loading logic
     */
    @Overwrite
    private void finalizeProviderLoading(List<GlyphProvider.Conditional> providers, GlyphProvider.Conditional fallback) {
        providers.add(0, fallback);
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
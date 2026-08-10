package me.drex.quickpack.mixin;

//? if >= 1.21.1 {
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
//? if >= 1.21.11 {
import com.mojang.blaze3d.font.UnbakedGlyph;
//? }
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(FontSet.class)
public abstract class FontSetMixin {
    @Shadow @Final
    private Int2ObjectMap<IntList> glyphsByWidth;

    /**
     * @author DrexHD
     * @reason Optimize font loading logic
     */
    @Overwrite
    private List<GlyphProvider> selectProviders(List<GlyphProvider.Conditional> providers, Set<FontOption> options) {
        IntSet claimedGlyphs = new IntOpenHashSet();
        List<GlyphProvider> usedProviders = new ArrayList<>();

        for (GlyphProvider.Conditional conditional : providers) {
            if (!conditional.filter().apply(options)) continue;

            GlyphProvider provider = conditional.provider();
            boolean used = false;
            for (int codepoint : provider.getSupportedGlyphs()) {
                if (claimedGlyphs.contains(codepoint)) continue;

                //? if >= 1.21.11 {
                UnbakedGlyph glyph = provider.getGlyph(codepoint);
                //? } else {
                /*GlyphInfo glyph = provider.getGlyph(codepoint);
                *///? }
                if (glyph == null) continue;

                claimedGlyphs.add(codepoint);
                used = true;
                //? if >= 1.21.11 {
                GlyphInfo glyphInfo = glyph.info();
                //? } else {
                /*GlyphInfo glyphInfo = glyph;
                *///? }
                if (glyphInfo != SpecialGlyphs.MISSING) {
                    int width = Mth.ceil(glyphInfo.getAdvance(false));
                    this.glyphsByWidth.computeIfAbsent(width, ignored -> new IntArrayList()).add(codepoint);
                }
            }

            if (used) usedProviders.add(provider);
        }

        return List.copyOf(usedProviders);
    }
}
//? } else {
/*import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class FontSetMixin {

}
*///? }
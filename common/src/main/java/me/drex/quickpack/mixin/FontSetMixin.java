package me.drex.quickpack.mixin;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(FontSet.class)
public abstract class FontSetMixin {
    /**
     * Vanilla's {@code consumer} probes every selected provider, in order, for each supported codepoint until one has
     * a glyph. Instead, iterate each provider's own glyphs once to find which provider claims a codepoint, and only
     * then run vanilla's consumer with {@code selectedProviders} narrowed to that single provider. The consumer (and
     * anything other mods inject into it) still runs once per codepoint with the same result, without the probing.
     */
    @WrapOperation(
        method = "selectProviders",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/ints/IntSet;forEach(Lit/unimi/dsi/fastutil/ints/IntConsumer;)V"
        )
    )
    private void fastSelectProviders(
        IntSet supportedGlyphs,
        IntConsumer consumer,
        Operation<Void> original,
        @Local(ordinal = 1) List<GlyphProvider> selectedProviders
    ) {
        List<GlyphProvider> providers = List.copyOf(selectedProviders);
        IntSet claimedGlyphs = new IntOpenHashSet(supportedGlyphs.size());

        try {
            for (GlyphProvider provider : providers) {
                selectedProviders.clear();
                selectedProviders.add(provider);

                for (int codepoint : provider.getSupportedGlyphs()) {
                    if (!supportedGlyphs.contains(codepoint) || claimedGlyphs.contains(codepoint)) continue;
                    if (provider.getGlyph(codepoint) == null) continue;

                    claimedGlyphs.add(codepoint);
                    consumer.accept(codepoint);
                }
            }
        } finally {
            selectedProviders.clear();
            selectedProviders.addAll(providers);
        }
    }
}
//? } else {
/*import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class FontSetMixin {

}
*///? }

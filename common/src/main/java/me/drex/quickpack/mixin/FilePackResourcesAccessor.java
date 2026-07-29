package me.drex.quickpack.mixin;

import net.minecraft.server.packs.FilePackResources;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FilePackResources.class)
public interface FilePackResourcesAccessor {
    @Accessor
    String getPrefix();

    @Accessor
    static Logger getLOGGER() {
        throw new AssertionError();
    }

}

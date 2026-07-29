package me.drex.quickpack.mixin;

import net.minecraft.server.packs.FilePackResources;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.zip.ZipFile;

@Mixin(FilePackResources.class)
public interface FilePackResourcesAccessor {
    @Accessor
    static Logger getLOGGER() {
        throw new AssertionError();
    }

    //? if >= 1.21.1 {
    @Accessor
    String getPrefix();

    //? } else {
     /*@Invoker
        ZipFile invokeGetOrCreateZipFile();
    *///? }
}

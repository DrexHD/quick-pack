package me.drex.quickpack.mixin;

import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.zip.ZipFile;

@Mixin(FilePackResources.SharedZipFileAccess.class)
public interface SharedZipFileAccessAccessor {
    @Invoker
    ZipFile invokeGetOrCreateZipFile();
}

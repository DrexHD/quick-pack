package me.drex.quickpack.mixin;

//? if >= 1.21.1 {
import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.zip.ZipFile;

@Mixin(FilePackResources.SharedZipFileAccess.class)
public interface SharedZipFileAccessAccessor {
    @Invoker
    ZipFile invokeGetOrCreateZipFile();
}
//? } else {

/*import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public interface SharedZipFileAccessAccessor {
}
*///? }

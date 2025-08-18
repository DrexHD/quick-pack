package me.drex.quickpack.mixin;

import me.drex.quickpack.packs.FastFilePackResources;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(FilePackResources.FileResourcesSupplier.class)
public abstract class FileResourcesSupplierMixin {
    @Shadow @Final private File content;

//    @Redirect(
//        method = "openFull",
//        at = @At(
//            value = "NEW",
//            target = "(Lnet/minecraft/server/packs/PackLocationInfo;L;Ljava/lang/String;)Lnet/minecraft/server/packs/FilePackResources;"
//        )
//    )
//    public PackResources useFastFilePackResources(PackLocationInfo packLocationInfo, FilePackResources.SharedZipFileAccess sharedZipFileAccess, String string) {
//        return new FastFilePackResources(packLocationInfo, sharedZipFileAccess, string);
//    }

    /**
     * @author drex
     * @reason Use FastFilePackResources, for some reason redirect doesn't work :/
     */
    @Overwrite
    public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
        FilePackResources.SharedZipFileAccess sharedZipFileAccess = new FilePackResources.SharedZipFileAccess(this.content);
        PackResources primaryPackResources = new FastFilePackResources(packLocationInfo, sharedZipFileAccess, "");
        List<String> overlays = metadata.overlays();
        if (overlays.isEmpty()) {
            return primaryPackResources;
        } else {
            List<PackResources> packResourcesStack = new ArrayList<>(overlays.size());

            for (String overlay : overlays) {
                packResourcesStack.add(new FastFilePackResources(packLocationInfo, sharedZipFileAccess, overlay));
            }

            return new CompositePackResources(primaryPackResources, packResourcesStack);
        }
    }
}

package me.drex.quickpack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.drex.quickpack.QuickPack;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(FilePackResources.FileResourcesSupplier.class)
public abstract class FileResourcesSupplierMixin {
    @Inject(method = "openFull", at = @At(value = "RETURN", ordinal = 0))
    public void initializeFileTree(
        PackLocationInfo location, Pack.Metadata metadata, CallbackInfoReturnable<PackResources> cir,
        @Local(name = "primary") PackResources primary,
        @Local(name = "fileAccess") FilePackResources.SharedZipFileAccess zipFileAccess
    ) {
        QuickPack.initializeFileTrees(zipFileAccess, List.of(primary));
    }

    @Inject(method = "openFull", at = @At(value = "RETURN", ordinal = 1))
    public void initializeFileTrees(
        PackLocationInfo location, Pack.Metadata metadata, CallbackInfoReturnable<PackResources> cir,
        @Local(name = "primary") PackResources primary,
        @Local(name = "overlayResources") List<PackResources> overlayResources,
        @Local(name = "fileAccess") FilePackResources.SharedZipFileAccess zipFileAccess
    ) {
        List<PackResources> packList = new ArrayList<>(overlayResources.size() + 1);
        packList.add(primary);
        packList.addAll(overlayResources);
        QuickPack.initializeFileTrees(zipFileAccess, packList);
    }
}

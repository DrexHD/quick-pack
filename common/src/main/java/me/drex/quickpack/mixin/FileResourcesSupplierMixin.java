package me.drex.quickpack.mixin;

//? if >= 1.21.1 {
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
        @Local PackResources primary,
        @Local FilePackResources.SharedZipFileAccess zipFileAccess
    ) {
        QuickPack.initializeFileTrees(((SharedZipFileAccessAccessor) zipFileAccess).invokeGetOrCreateZipFile(), List.of(primary));
    }

    @Inject(method = "openFull", at = @At(value = "RETURN", ordinal = 1))
    public void initializeFileTrees(
        PackLocationInfo location, Pack.Metadata metadata, CallbackInfoReturnable<PackResources> cir,
        @Local PackResources primary,
        @Local(ordinal = 1) List<PackResources> overlayResources,
        @Local FilePackResources.SharedZipFileAccess zipFileAccess
    ) {
        List<PackResources> packList = new ArrayList<>(overlayResources.size() + 1);
        packList.add(primary);
        packList.addAll(overlayResources);
        QuickPack.initializeFileTrees(((SharedZipFileAccessAccessor) zipFileAccess).invokeGetOrCreateZipFile(), packList);
    }
}
//? } else {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.drex.quickpack.QuickPack;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(FolderRepositorySource.class)
public abstract class FileResourcesSupplierMixin {
    @ModifyReturnValue(
        method = {
            //? if >= 1.20.1 {
            "method_45268", // fabric
            "lambda$detectPackResources$2" // forge
            //? } else {
            /^"method_14434", // fabric
            "lambda$createSupplier$1" // forge
            ^///? }
        },
        at = @At("TAIL")
    )
    private static PackResources initializeFileTrees(PackResources original) {
        QuickPack.initializeFileTrees(((FilePackResourcesAccessor) original).invokeGetOrCreateZipFile(), List.of(original));
        return original;
    }
}
*///? }
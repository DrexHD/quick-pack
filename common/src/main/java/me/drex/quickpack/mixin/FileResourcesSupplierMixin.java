package me.drex.quickpack.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.drex.quickpack.packs.FastFilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;

@Mixin(FolderRepositorySource.class)
public abstract class FileResourcesSupplierMixin {

    @ModifyReturnValue(
        method = {
            "method_14434", // fabric
            "lambda$createSupplier$1" // forge
        },
        at = @At("TAIL")
    )
    private static PackResources useFastFilePackResources(
        PackResources original, @Local(argsOnly = true) File file
    ) {
        return new FastFilePackResources(file);
    }
}

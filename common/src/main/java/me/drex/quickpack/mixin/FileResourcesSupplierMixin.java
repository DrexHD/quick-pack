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
            "method_45268", // fabric
            "lambda$detectPackResources$2" // forge
        },
        at = @At("TAIL")
    )
    private static PackResources useFastFilePackResources(
        PackResources original, @Local(argsOnly = true) String name, @Local(argsOnly = true) File file,
        @Local(argsOnly = true) boolean isBuiltin
    ) {
        return new FastFilePackResources(name, file, isBuiltin);
    }
}

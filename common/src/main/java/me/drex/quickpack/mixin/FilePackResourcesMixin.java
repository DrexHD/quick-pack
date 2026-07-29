package me.drex.quickpack.mixin;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.drex.quickpack.duck.IFilePackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin implements IFilePackResources {
    //? if >= 1.21.1 {
    @Shadow
    protected abstract String addPrefix(String path);
    //? }

    @Unique
    private TreeSet<String> quick_pack$fileTree = null;
    @Unique
    private Map<String, Set<String>> quick_pack$namespaces = null;

    @Override
    public void quick_pack$initializeFileTree(TreeSet<String> fileTree, Map<String, Set<String>> namespaces) {
        this.quick_pack$fileTree = fileTree;
        this.quick_pack$namespaces = namespaces;
    }

    @WrapOperation(
        method = "listResources",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/zip/ZipFile;entries()Ljava/util/Enumeration;"
        )
    )
    public Enumeration<? extends ZipEntry> fastListResources(
        ZipFile instance,
        Operation<Enumeration<? extends ZipEntry>> original,
        @Local(argsOnly = true) PackType packType,
        @Local(ordinal = 0, argsOnly = true) String namespace,
        @Local(ordinal = 1, argsOnly = true) String directory
    ) {
        if (quick_pack$fileTree == null) {
            return original.call(instance);
        }
        //? if >= 1.21.1 {
        String root = this.addPrefix(packType.getDirectory() + "/" + namespace + "/");
        //? } else {
        /*String root = packType.getDirectory() + "/" + namespace + "/";
        *///? }
        String prefix = root + directory + "/";

        return Iterators.asEnumeration(
            Iterators.transform(
                quick_pack$fileTree.subSet(prefix, prefix + Character.MAX_VALUE).iterator(),
                instance::getEntry
            )
        );
    }

    @WrapMethod(method = "getNamespaces")
    public Set<String> fastGetNamespaces(PackType packType, Operation<Set<String>> original) {
        if (quick_pack$namespaces == null) {
            return original.call(packType);
        }
        return this.quick_pack$namespaces.getOrDefault(packType.getDirectory(), Collections.emptySet());
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void onClose(CallbackInfo ci) {
        quick_pack$fileTree = null;
        quick_pack$namespaces = null;
    }
}

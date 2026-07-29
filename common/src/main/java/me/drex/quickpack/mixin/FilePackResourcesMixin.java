package me.drex.quickpack.mixin;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.sugar.Local;
import me.drex.quickpack.duck.IFilePackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin implements IFilePackResources {
    @Shadow
    protected abstract String addPrefix(String path);

    @Unique
    private TreeSet<String> quick_pack$fileTree = null;
    @Unique
    private Map<String, Set<String>> quick_pack$namespaces = null;

    @Override
    public void quick_pack$initializeFileTree(TreeSet<String> fileTree, Map<String, Set<String>> namespaces) {
        this.quick_pack$fileTree = fileTree;
        this.quick_pack$namespaces = namespaces;
    }

    @Redirect(
        method = "listResources",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/zip/ZipFile;entries()Ljava/util/Enumeration;"
        )
    )
    public Enumeration<? extends ZipEntry> fastListResources(
        ZipFile instance,
        @Local(argsOnly = true) PackType packType,
        @Local(ordinal = 0, argsOnly = true) String namespace,
        @Local(ordinal = 1, argsOnly = true) String directory
    ) {
        if (quick_pack$fileTree == null) {
            return instance.entries();
        }
        String root = this.addPrefix(packType.getDirectory() + "/" + namespace + "/");
        String prefix = root + directory + "/";

        return Iterators.asEnumeration(
            Iterators.transform(
                quick_pack$fileTree.subSet(prefix, prefix + Character.MAX_VALUE).iterator(),
                instance::getEntry
            )
        );
    }

    @Redirect(
        method = "getNamespaces",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/zip/ZipFile;entries()Ljava/util/Enumeration;"
        )
    )
    public Enumeration<? extends ZipEntry> fastGetNamespaces(ZipFile instance, PackType packType) {
        if (quick_pack$namespaces == null) {
            return instance.entries();
        }
        return Collections.emptyEnumeration();
    }

    @Inject(
        method = "getNamespaces",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/FilePackResources;addPrefix(Ljava/lang/String;)Ljava/lang/String;"
        )
    )
    public void fastGetNamespaces(
        PackType packType,
        CallbackInfoReturnable<Set<String>> cir,
        @Local(name = "namespaces") Set<String> namespaces
    ) {
        if (quick_pack$namespaces == null) return;
        namespaces.addAll(this.quick_pack$namespaces.getOrDefault(packType.getDirectory(), Collections.emptySet()));
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void onClose(CallbackInfo ci) {
        quick_pack$fileTree = null;
        quick_pack$namespaces = null;
    }
}

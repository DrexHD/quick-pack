package me.drex.quickpack;

import me.drex.quickpack.duck.IFilePackResources;
import me.drex.quickpack.mixin.FilePackResourcesAccessor;
import me.drex.quickpack.mixin.SharedZipFileAccessAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class QuickPack {
    public static final String MOD_ID = "quick-pack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initializeFileTrees(FilePackResources.SharedZipFileAccess zipFileAccess, List<PackResources> packList) {
        ZipFile zipFile = ((SharedZipFileAccessAccessor) zipFileAccess).invokeGetOrCreateZipFile();
        if (zipFile == null) return;
        Map<String, PackResources> packsByPrefix = new HashMap<>();
        for (PackResources packResource : packList) {
            if (packResource instanceof FilePackResourcesAccessor accessor) {
                String prefix = accessor.getPrefix();
                packsByPrefix.put(prefix, packResource);
            } else {
                LOGGER.warn("Non-file pack {} in pack list, ignoring", packResource);
            }
        }

        Map<String, TreeSet<String>> treeSetByPrefix = new HashMap<>();
        Map<String, Map<String, Set<String>>> nameSpacesByPrefix = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;

            String path = entry.getName();
            String[] parts = path.split("/");
            if (parts.length == 0) continue;
            boolean isOverlay = packsByPrefix.containsKey(parts[0]);
            String prefix, type, namespace;
            if (isOverlay && parts.length >= 3) {
                prefix = parts[0];
                type = parts[1];
                namespace = parts[2];
            } else if (!isOverlay && parts.length >= 2) {
                prefix = "";
                type = parts[0];
                namespace = parts[1];
            } else {
                continue;
            }

            if (Identifier.isValidNamespace(namespace)) {
                nameSpacesByPrefix.computeIfAbsent(prefix, s -> new HashMap<>())
                    .computeIfAbsent(type, s -> new HashSet<>()).add(namespace);
            } else {
                FilePackResourcesAccessor.getLOGGER().warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", namespace, zipFile);
            }

            treeSetByPrefix.computeIfAbsent(prefix, s -> new TreeSet<>()).add(path);
        }

        packsByPrefix.forEach((prefix, packResource) ->
            ((IFilePackResources) packResource).quick_pack$initializeFileTree(
                treeSetByPrefix.getOrDefault(prefix, new TreeSet<>()),
                nameSpacesByPrefix.getOrDefault(prefix, new HashMap<>())
            )
        );
    }

}

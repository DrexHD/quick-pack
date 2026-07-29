package me.drex.quickpack;

import me.drex.quickpack.duck.IFilePackResources;
import me.drex.quickpack.mixin.FilePackResourcesAccessor;
//? if >= 1.21.1 {
import net.minecraft.resources.Identifier;
//? } else {
/*import java.util.Locale;
*///? }
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

    public static void initializeFileTrees(ZipFile zipFile, List<PackResources> packList) {
        if (zipFile == null) return;
        Map<String, PackResources> packsByPrefix = new HashMap<>();
        for (PackResources packResource : packList) {
            if (packResource instanceof FilePackResourcesAccessor accessor) {
                //? if >= 1.21.1 {
                String prefix = accessor.getPrefix();
                //? } else {
                /*String prefix = "";
                *///? }
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

            //? if >= 1.21.1 {
            if (Identifier.isValidNamespace(namespace)) {
            //? } else {
            /*if (namespace.equals(namespace.toLowerCase(Locale.ROOT))) {
            *///? }
                nameSpacesByPrefix.computeIfAbsent(prefix, s -> new HashMap<>())
                    .computeIfAbsent(type, s -> new HashSet<>()).add(namespace);
            } else {
                //? if >= 1.21.1 {
                FilePackResourcesAccessor.getLOGGER().warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", namespace, zipFile);
                 //? } else {
                /*FilePackResourcesAccessor.getLOGGER().warn("Ignored non-lowercase namespace: {} in {}", namespace, zipFile);
                *///? }
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

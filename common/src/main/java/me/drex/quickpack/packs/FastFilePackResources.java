package me.drex.quickpack.packs;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FastFilePackResources extends AbstractPackResources {
    public static final Logger LOGGER = LogUtils.getLogger();

    private TreeSet<String> fileTree = new TreeSet<>();
    private Map<String, Set<String>> namespaces = new HashMap<>();
    private ZipFile zipFile = null;
    private final List<String> prefixStack;
    private final Set<String> overlays = Collections.emptySet();
    private boolean extracted = false;

    public FastFilePackResources(String name, File file, boolean isBuiltin) {
        super(name, isBuiltin);
        try {
            this.zipFile = new ZipFile(file);
        } catch (IOException e) {
            LOGGER.error("Failed to open pack {}", file, e);
        }

        prefixStack = new ArrayList<>(1);
        prefixStack.add("");
    }

    private void ensureFileTree() {
        if (extracted) return;
        extracted = true;
        if (zipFile == null) {
            return;
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            String path = entry.getName();
            if (entry.isDirectory()) {
                path = path.substring(0, path.length() - 1);
            }

            extractNamespace(path);

            fileTree.add(path);
        }
    }

    private void extractNamespace(String path) {
        String[] parts = path.split("/");
        if (parts.length == 0) return;

        boolean isOverlay = overlays.contains(parts[0]);
        String type;
        String namespace;
        if (isOverlay && parts.length >= 3) {
            type = parts[1];
            namespace = parts[2];
        } else if (!isOverlay && parts.length >= 2) {
            type = parts[0];
            namespace = parts[1];
        } else {
            return;
        }

        if (namespace.equals(namespace.toLowerCase(Locale.ROOT))) {
            namespaces.computeIfAbsent(type, s -> new HashSet<>()).add(namespace);
        } else {
            LOGGER.warn("Ignored non-lowercase namespace: {} in {}", namespace, zipFile);
        }
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... parts) {
        return getResource(String.join("/", parts));
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        for (String prefix : prefixStack) {
            IoSupplier<InputStream> supplier = getResource(prefix + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
            if (supplier == null) continue;
            return supplier;

        }
        return null;
    }

    private IoSupplier<InputStream> getResource(String path) {
        if (zipFile == null) {
            return null;
        }
        ZipEntry entry = zipFile.getEntry(path);
        if (entry == null) {
            return null;
        }
        return IoSupplier.create(zipFile, entry);
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        ensureFileTree();
        Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap<>();

        for (String prefix : prefixStack) {
            String namespacePrefix = prefix + packType.getDirectory() + "/" + namespace + "/";
            String dirPrefix = namespacePrefix + path + "/";
            String end = dirPrefix + Character.MAX_VALUE;
            fileTree.subSet(dirPrefix, end).forEach((filePath) -> {
                String rlPath = filePath.substring(namespacePrefix.length());
                ResourceLocation location = ResourceLocation.tryBuild(namespace, rlPath);
                if (location != null) {
                    map.putIfAbsent(location, getResource(filePath));
                } else {
                    LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, rlPath);
                }
            });
        }
        map.forEach(resourceOutput);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        ensureFileTree();
        return namespaces.getOrDefault(packType.getDirectory(), Collections.emptySet());
    }

    @Override
    public void close() {
        if (zipFile != null) {
            IOUtils.closeQuietly(this.zipFile);
            zipFile = null;
            namespaces = null;
            fileTree = null;
        }
    }
}

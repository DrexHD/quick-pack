package me.drex.quickpack.packs;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FastFilePackResources extends AbstractPackResources {
    public static final Logger LOGGER = LogUtils.getLogger();

    private final TreeMap<String, byte[]> fileMap = new TreeMap<>();
    private final Map<String, Set<String>> namespaces = new HashMap<>();
    private ZipFile zipFile = null;
    private final List<String> prefixStack;
    private final Set<String> overlays = Collections.emptySet();

    public FastFilePackResources(String name, File file, boolean isBuiltin) {
        super(name, isBuiltin);
        try {
            this.zipFile = new ZipFile(file);
        } catch (IOException e) {
            LOGGER.error("Failed to open pack {}", file, e);
        }

        prefixStack = new ArrayList<>(1);
        prefixStack.add("");

        extractFiles();
    }

    public void extractFiles() {
        if (zipFile == null) return;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;

            String path = entry.getName();
            extractNamespace(path);

            try (InputStream in = zipFile.getInputStream(entry)) {
                byte[] data = in.readAllBytes();
                fileMap.put(path, data);
            } catch (IOException e) {
                FastFilePackResources.LOGGER.error("Failed to load resource {}", path, e);
            }
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
        byte[] data = fileMap.get(String.join("/", parts));
        if (data == null) return null;
        return () -> new ByteArrayInputStream(data);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        for (String prefix : prefixStack) {
            byte[] data = fileMap.get(prefix + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
            if (data == null) continue;
            return () -> new ByteArrayInputStream(data);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap<>();

        for (String prefix : prefixStack) {
            String namespacePrefix = prefix + packType.getDirectory() + "/" + namespace + "/";
            String dirPrefix = namespacePrefix + path + "/";
            String end = dirPrefix + Character.MAX_VALUE;
            fileMap.subMap(dirPrefix, end).forEach((filePath, bytes) -> {
                String rlPath = filePath.substring(namespacePrefix.length());
                ResourceLocation location = ResourceLocation.tryBuild(namespace, rlPath);
                if (location != null) {
                    map.putIfAbsent(location, () -> new ByteArrayInputStream(bytes));
                } else {
                    LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, rlPath);
                }
            });
        }
        map.forEach(resourceOutput);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return namespaces.getOrDefault(packType.getDirectory(), Collections.emptySet());
    }

    @Override
    public void close() {
        if (zipFile != null) {
            IOUtils.closeQuietly(this.zipFile);
        }
    }
}

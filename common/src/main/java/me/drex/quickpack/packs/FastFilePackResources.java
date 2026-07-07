package me.drex.quickpack.packs;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
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

    public FastFilePackResources(File file) {
        super(file);
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
    public Collection<ResourceLocation> getResources(PackType packType, String namespace, String path, Predicate<ResourceLocation> predicate) {
        ensureFileTree();
        List<ResourceLocation> list = new LinkedList<>();

        for (String prefix : prefixStack) {
            String namespacePrefix = prefix + packType.getDirectory() + "/" + namespace + "/";
            String dirPrefix = namespacePrefix + path + "/";
            String end = dirPrefix + Character.MAX_VALUE;
            fileTree.subSet(dirPrefix, end).forEach((filePath) -> {
                if (zipFile != null) {
                    ZipEntry zipEntry = zipFile.getEntry(filePath);
                    if (zipEntry != null && zipEntry.isDirectory()) {
                        return;
                    }
                }
                if (filePath.endsWith(".mcmeta")) return;

                String rlPath = filePath.substring(namespacePrefix.length());
                ResourceLocation location = ResourceLocation.tryBuild(namespace, rlPath);
                if (location == null) {
                    LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, rlPath);
                } else if (predicate.test(location)) {
                    list.add(location);
                }
            });
        }
        return list;
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

    @Override
    protected InputStream getResource(String string) throws IOException {
        if (zipFile != null) {
            for (String prefix : prefixStack) {
                ZipEntry entry = zipFile.getEntry(prefix + string);
                return zipFile.getInputStream(entry);
            }
        }
        throw new ResourcePackFileNotFoundException(this.file, string);
    }

    @Override
    protected boolean hasResource(String string) {
        for (String prefix : prefixStack) {
            boolean contained = fileTree.contains(prefix + string);
            if (contained) return true;
        }
        return false;
    }
}

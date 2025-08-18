package me.drex.quickpack.packs;

import com.mojang.logging.LogUtils;
import me.drex.quickpack.mixin.SharedZipFileAccessAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FastFilePackResources extends AbstractPackResources {
    static final Logger LOGGER = LogUtils.getLogger();

    private final TreeMap<String, byte[]> fileMap = new TreeMap<>();

    private final Map<String, Set<String>> namespaces = new HashMap<>();
    private final FilePackResources.SharedZipFileAccess sharedZipFileAccess;
    private final String prefix;

    public FastFilePackResources(PackLocationInfo packLocationInfo, FilePackResources.SharedZipFileAccess sharedZipFileAccess, String prefix) {
        super(packLocationInfo);
        this.sharedZipFileAccess = sharedZipFileAccess;
        this.prefix = prefix;
        extractFiles(sharedZipFileAccess);
    }

    public void extractFiles(FilePackResources.SharedZipFileAccess sharedZipFile) {
        ZipFile zipFile = ((SharedZipFileAccessAccessor) sharedZipFile).invokeGetOrCreateZipFile();
        if (zipFile == null) return;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;

            String path = entry.getName();
            String[] parts = path.split("/");

            if (!prefix.isEmpty()) {
                if (parts.length == 0) continue;
                if (!parts[0].equals(prefix)) continue;
            }

            int startIndex;
            if (prefix.isEmpty()) {
                startIndex = 0;
            } else {
                startIndex = 1;
            }
            if (parts.length > startIndex + 2) {
                String type = parts[startIndex];
                String namespace = parts[startIndex + 1];
                if (ResourceLocation.isValidNamespace(namespace)) {
                    namespaces.computeIfAbsent(type, s -> new HashSet<>()).add(namespace);
                } else {
                    LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", namespace, ((SharedZipFileAccessAccessor) sharedZipFile).getFile());
                }
            }

            try (InputStream in = zipFile.getInputStream(entry)) {
                byte[] data = in.readAllBytes();
                fileMap.put(path, data);
            } catch (IOException e) {
                FastFilePackResources.LOGGER.error("Failed to load resource {}", path, e);
            }
        }
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... parts) {
        return getResource(String.join("/", parts));
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        return getResource(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
    }

    private String addPrefix(String string) {
        return this.prefix.isEmpty() ? string : this.prefix + "/" + string;
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String filePath) {
        byte[] data = fileMap.get(addPrefix(filePath));
        if (data == null) return null;
        return () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        String namespacePrefix = addPrefix(packType.getDirectory() + "/" + namespace + "/");
        String dirPrefix = namespacePrefix + path + "/";
        String end = dirPrefix + Character.MAX_VALUE;
        fileMap.subMap(dirPrefix, end).forEach((filePath, bytes) -> {
            String rlPath = filePath.substring(namespacePrefix.length());
            ResourceLocation location = ResourceLocation.tryBuild(namespace, rlPath);
            if (location != null) {
                resourceOutput.accept(location, () -> new ByteArrayInputStream(bytes));
            } else {
                LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, rlPath);
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return namespaces.getOrDefault(packType.getDirectory(), Collections.emptySet());
    }

    @Override
    public void close() {
        sharedZipFileAccess.close();
    }
}

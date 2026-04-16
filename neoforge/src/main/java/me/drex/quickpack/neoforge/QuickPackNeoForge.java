package me.drex.quickpack.neoforge;

import me.drex.quickpack.config.ConfigManager;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod("quick_pack")
public class QuickPackNeoForge {
    public QuickPackNeoForge() {
        ConfigManager.load(FMLPaths.CONFIGDIR.get());
    }
}

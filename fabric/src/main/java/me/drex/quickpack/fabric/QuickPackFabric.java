package me.drex.quickpack.fabric;

import me.drex.quickpack.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class QuickPackFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.load(FabricLoader.getInstance().getConfigDir());
    }
}

package cn.lyricraft.lyricore.config.helper;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public class ConfigHelperRegistrar {
    private final String namespace;

    public ConfigHelperRegistrar(String namespace){
        this.namespace = namespace;
    }

    public void register(ModConfig.Type type, @NotNull ModConfigSpec config){
        String typeName = ConfigHelper.typeEnumToString(type);
        if (typeName.isEmpty())
            throw new IllegalArgumentException("Cannot register config: invalid config type.");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, typeName);
        if (ConfigHelper.getConfigSpec(id) != null)
            throw new IllegalArgumentException("Cannot register config: a config with the same id already exists.");
        ConfigHelper.registerConfig(id, config);
    }
}

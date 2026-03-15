package cn.lyricraft.lyricore.config.helper;

import cn.lyricraft.lyricore.Lyricore;
import com.electronwill.nightconfig.core.ConfigSpec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigHelper {
    private static final Map<ResourceLocation, ModConfigSpec> configSpecs = new HashMap<>();
    public static ModConfig.Type typeStringToEnum(String string){
        switch (string){
            case "common" -> {
                return ModConfig.Type.COMMON;
            }
            case "client" -> {
                return ModConfig.Type.CLIENT;
            }
            case "server" -> {
                return ModConfig.Type.SERVER;
            }
            case "startup" -> {
                return ModConfig.Type.STARTUP;
            }
            default -> {
                return null;
            }
        }
    }

    public static String typeEnumToString(ModConfig.Type type){
        switch (type){
            case COMMON -> {
                return "common";
            }
            case CLIENT -> {
                return "client";
            }
            case SERVER -> {
                return "server";
            }
            case STARTUP -> {
                return "startup";
            }
            default -> {
                return "";
            }
        }
    }

    public static ConfigHelperRegistrar getRegistrar(String namespace){
        return new ConfigHelperRegistrar(namespace);
    }

    protected static void registerConfig(ResourceLocation id, ModConfigSpec config){
        configSpecs.put(id, config);
    }

    public static ModConfigSpec getConfigSpec(ResourceLocation id){
        ModConfigSpec config = configSpecs.get(id);
        if (config == null){
            Lyricore.LOGGER.warn("[ConfigHelper] 该配置未被注册 / No config registered named: " + id);
            return null;
        }
        return config;
    }

    public static ModConfigSpec.ConfigValue getConfigValueFromLocation(ResourceLocation location) {
        String path = location.getPath();
        List<String> segments = List.of(path.split("\\."));
        if (segments.size() < 2) {
            Lyricore.LOGGER.warn("[ConfigHelper] 无效的配置路径 / Invalid config path: " + path);
            return null;
        }
        ModConfigSpec configSpec = getConfigSpec(
                ResourceLocation.fromNamespaceAndPath(location.getNamespace(), segments.getFirst())
        );
        if (configSpec == null) {
            return null;
        }
        segments.removeFirst(); // 现在是配置值的路径了
        ModConfigSpec.ConfigValue value = configSpec.getValues().get(segments);
        if (value == null) {
            Lyricore.LOGGER.warn("[ConfigHelper] 无法找到配置值 / Config value not found for path: " + path);
            return null;
        }
        return value;
    }
}

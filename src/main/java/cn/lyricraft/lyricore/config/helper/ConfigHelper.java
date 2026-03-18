package cn.lyricraft.lyricore.config.helper;

import cn.lyricraft.lyricore.Lyricore;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigHelper {
    private static final BiMap<ResourceLocation, ModConfigSpec> configSpecs = HashBiMap.create();
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

    public static ModConfig.Type typeOfPath(String path) {
        String[] locationSegments = path.split(":");
        if (locationSegments.length != 2) return null;
        String[] pathSegments = locationSegments[1].split("\\.");
        if (pathSegments.length < 1) return null;
        return typeStringToEnum(pathSegments[0]);
    }

    protected static void registerConfig(ResourceLocation id, ModConfigSpec config){
        configSpecs.put(id, config);
    }

    protected static ModConfigSpec getConfigSpec(ResourceLocation id, boolean isTry){
        ModConfigSpec config = configSpecs.get(id);
        if (config == null){
            if (!isTry)
                Lyricore.LOGGER.warn("[ConfigHelper] 该配置未被注册 / No config registered named: " + id);
            return null;
        }
        return config;
    }

    public static ModConfigSpec getConfigSpec(ResourceLocation id){
        return getConfigSpec(id, false);
    }

    public static ModConfigSpec.ConfigValue getConfigValueFromPath(String location) {
        String[] locationSegments = location.split(":");
        if (locationSegments.length != 2
                || locationSegments[0].isEmpty() || locationSegments[1].isEmpty()){
            Lyricore.LOGGER.warn("[ConfigHelper] 无效的配置路径 / Invalid config path: " + location);
            return null;
        }
        List<String> pathSegments = new ArrayList<>(List.of(locationSegments[1].split("\\.")));
        if (pathSegments.size() < 2) {
            Lyricore.LOGGER.warn("[ConfigHelper] 无效的配置路径 / Invalid config path: " + location);
            return null;
        }
        ModConfigSpec configSpec = getConfigSpec(
                ResourceLocation.fromNamespaceAndPath(locationSegments[0], pathSegments.getFirst())
        );
        if (configSpec == null) {
            return null;
        }
        pathSegments.removeFirst(); // 现在是配置值的路径了
        ModConfigSpec.ConfigValue value = configSpec.getValues().get(pathSegments);
        if (value == null) {
            Lyricore.LOGGER.warn("[ConfigHelper] 无法找到配置值 / Config value not found for path: " + location);
            return null;
        }
        return value;
    }

    public static String getConfigPathFromValue(ModConfigSpec configSpec, ModConfigSpec.ConfigValue configValue) {
        ResourceLocation location = configSpecs.inverse().get(configSpec);
        if (location == null) {
            Lyricore.LOGGER.warn("[ConfigHelper] 该配置未被注册 / No config registered likes: " + configSpec.toString());
            return "";
        }
        return location.toString() + "." + String.join(".", configValue.getPath());
    }

    public static I18n i18n(ModConfigSpec configSpec){
        ResourceLocation location = configSpecs.inverse().get(configSpec);
        if (location == null) {
            Lyricore.LOGGER.warn("[ConfigHelper] 该配置未被注册 / No config registered likes: " + configSpec.toString());
            throw new IllegalArgumentException("No config registered likes: " + configSpec);
        }
        return new I18n(location);
    }

    public static class I18n{
        private ResourceLocation specsLocation;

        private I18n(ResourceLocation specsLocation){
            this.specsLocation = specsLocation;
        }

        public static String title(String modId){
            return (modId + ".configuration.title");
        }

        private String head(){
            return (specsLocation.getNamespace()+".configuration.");
        }

        public String file(){
            return (head() + "section." + specsLocation.getNamespace() + "." + specsLocation.getPath() + ".toml");
        }

        public String fileTitle(){
            return (file() + ".title");
        }

        public String section(String name){
            return (head() + name);
        }

        public String sectionTooltip(String name){
            return (section(name) + ".tooltip");
        }

        public String sectionButton(String name){
            return (section(name) + ".button");
        }

        // Config Value
        public String cv(ModConfigSpec.ConfigValue value){
            return (head() + value.getPath().getLast());
        }

        public String cvTooltip(ModConfigSpec.ConfigValue value){
            return (cv(value) + ".tooltip");
        }

    }
}

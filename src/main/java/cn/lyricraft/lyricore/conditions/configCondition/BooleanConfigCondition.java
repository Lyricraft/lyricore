package cn.lyricraft.lyricore.conditions.configCondition;

import cn.lyricraft.lyricore.config.helper.ConfigHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record BooleanConfigCondition(String path, ModConfigSpec.BooleanValue config) implements ICondition {
    public static final MapCodec<BooleanConfigCondition> CODEC =
            Codec.STRING.comapFlatMap(location -> {
                ModConfig.Type type = ConfigHelper.typeOfPath(location);
                if (type == null) return DataResult.error(() ->
                        "Invalid path parameter for lyricore:boolean_config condition: " + location);
                if (type == ModConfig.Type.SERVER) return DataResult.error(() ->
                        "Server configs are not supported for lyricore:boolean_config condition: " + location);
                ModConfigSpec.ConfigValue value = ConfigHelper.getConfigValueFromPath(location);
                if (value == null) return DataResult.error(() ->
                        "Config value not found for path parameter in lyricore:boolean_config condition: "
                                + location);
                if (value instanceof ModConfigSpec.BooleanValue boolValue)
                    return DataResult.success(new BooleanConfigCondition(location, boolValue));
                else return DataResult.error(() ->
                        "Config value at path parameter is not a boolean for lyricore:boolean_config condition: "
                                + location);
            }, BooleanConfigCondition::path).fieldOf("path");

    @Override
    public boolean test(@NotNull IContext context) {
        return config.isTrue();
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public static BooleanConfigCondition of(ModConfigSpec spec, ModConfigSpec.BooleanValue config){
        return new BooleanConfigCondition(ConfigHelper.getConfigPathFromValue(spec, config), config);
    }
}

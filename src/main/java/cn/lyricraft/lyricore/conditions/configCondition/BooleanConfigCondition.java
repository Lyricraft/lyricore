package cn.lyricraft.lyricore.conditions.configCondition;

import cn.lyricraft.lyricore.config.helper.ConfigHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record BooleanConfigCondition(ResourceLocation location, ModConfigSpec.BooleanValue config) implements ICondition {
    public static final MapCodec<BooleanConfigCondition> CODEC =
            Codec.STRING.comapFlatMap(location -> {
                String[] locationSegments = location.split(":");
                if (locationSegments.length != 2) return DataResult.error(() ->
                        "Invalid location parameter for lyricore:boolean_config condition: " + location);
                ResourceLocation locationObj;
                try{
                    locationObj = ResourceLocation.fromNamespaceAndPath(locationSegments[0], locationSegments[1]);
                } catch (ResourceLocationException e){
                    return DataResult.error(() ->
                            "Invalid location parameter for lyricore:boolean_config condition: " + location);
                }
                ModConfigSpec.ConfigValue value = ConfigHelper.getConfigValueFromLocation(locationObj);
                if (value == null) return DataResult.error(() ->
                        "Config value not found for location parameter in lyricore:boolean_config condition: "
                                + location);
                if (value instanceof ModConfigSpec.BooleanValue boolValue)
                    return DataResult.success(new BooleanConfigCondition(locationObj, boolValue));
                else return DataResult.error(() ->
                        "Config value at location parameter is not a boolean for lyricore:boolean_config condition: "
                                + location);
            }, condition -> condition.location().toString()).fieldOf("location");

    @Override
    public boolean test(@NotNull IContext context) {
        return config.isTrue();
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}

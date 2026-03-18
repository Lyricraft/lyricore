package cn.lyricraft.lyricore.conditions;

import cn.lyricraft.lyricore.Lyricore;
import cn.lyricraft.lyricore.conditions.configCondition.BooleanConfigCondition;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class LCConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Lyricore.MOD_ID);

    public static void register(IEventBus eventBus){
        CONDITION_CODECS.register(eventBus);
    }

    public static final Supplier<MapCodec<BooleanConfigCondition>> BOOLEAN_CONFIG =
            CONDITION_CODECS.register("boolean_config", () -> BooleanConfigCondition.CODEC);
}

package cn.lyricraft.lyricore;

import cn.lyricraft.lyricore.network.requestManager.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Lyricore.MOD_ID)
public class Lyricore {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "lyricore";
    public static final String MOD_NAMESPACE = MOD_ID;
    public static final String MOD_VERSION = "0.9.0";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // 注册通用 RequestManager
    public static final ServerRequestManagerRegistrar SERVER_REQUEST_MANAGERS = new ServerRequestManagerRegistrar(MOD_VERSION);
    public static final ServerRequestManager SERVER_REQUEST_MANAGER = SERVER_REQUEST_MANAGERS.register(
            new ServerRequestManager().idStrict(),
            ManagedResponsePayload.TYPE,
            ManagedResponsePayload.STREAM_CODEC);
    public static final ServerResponseManager SERVER_RESPONSE_MANAGER = SERVER_REQUEST_MANAGERS.register(
            new ServerResponseManager().strict(),
            ManagedRequestPayload.TYPE,
            ManagedRequestPayload.STREAM_CODEC);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Lyricore(IEventBus modEventBus, ModContainer modContainer) {

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // 真注册通用 RequestManager
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        SERVER_REQUEST_MANAGERS.register(event);
    }


}

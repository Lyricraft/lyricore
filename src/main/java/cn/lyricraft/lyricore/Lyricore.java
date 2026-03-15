package cn.lyricraft.lyricore;

import cn.lyricraft.lyricore.conditions.AllConditions;
import cn.lyricraft.lyricore.network.requestManager.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Lyricore.MOD_ID)
@EventBusSubscriber(modid = Lyricore.MOD_ID)
public class Lyricore {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "lyricore";
    public static final String MOD_NAMESPACE = MOD_ID;
    public static final String MOD_VERSION = "0.1.0";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // 注册通用 RequestManager
    public static final ServerRequestManagerRegistrar SERVER_REQUEST_MANAGERS = new ServerRequestManagerRegistrar(
            MOD_ID + " " + MOD_VERSION);
    public static final ServerRequestManager SERVER_REQUEST_MANAGER = SERVER_REQUEST_MANAGERS.register(
            new ServerRequestManager(AbstractRequestManager.DEFAULT_NAME,
                    AbstractRequestManager.DEFAULT_TIMEOUT,
                    AbstractRequestManager.DEFAULT_HANDLE_EXPIRED_INTERVAL)
                    .idStrict());
    public static final ServerResponseManager SERVER_RESPONSE_MANAGER = SERVER_REQUEST_MANAGERS.register(
            new ServerResponseManager(AbstractRequestManager.DEFAULT_NAME).strict());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Lyricore(IEventBus modEventBus, ModContainer modContainer) {
        // 注册所有数据加载条件
        AllConditions.register(modEventBus);
        // 将 ServerRequestManagerRegistrar 注册到模组事件总线
        // modEventBus.register(SERVER_REQUEST_MANAGERS);
        // 将本类注册到全局事件总线
        // NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        // LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        // LOGGER.info("HELLO from server starting");
    }

    // 真注册通用 RequestManager
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        SERVER_REQUEST_MANAGERS.register(event);
    }

}

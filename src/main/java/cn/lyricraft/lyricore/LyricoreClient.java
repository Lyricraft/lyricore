package cn.lyricraft.lyricore;

import cn.lyricraft.lyricore.network.requestManager.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Lyricore.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Lyricore.MOD_ID, value = Dist.CLIENT)
public class LyricoreClient {



    public LyricoreClient(ModContainer container) {

    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        // Lyricore.LOGGER.info("HELLO FROM CLIENT SETUP");
        // Lyricore.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    // 真注册通用 RequestManager
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {

    }
}

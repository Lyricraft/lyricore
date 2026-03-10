package cn.lyricraft.lyricore;

import cn.lyricraft.lyricore.network.requestManager.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Lyricore.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Lyricore.MOD_ID, value = Dist.CLIENT)
public class LyricoreClient {

    // 注册通用 RequestManager
    public static final ClientRequestManagerRegistrar CLIENT_REQUEST_MANAGERS = new ClientRequestManagerRegistrar(Lyricore.MOD_VERSION);
    public static final ClientRequestManager CLIENT_REQUEST_MANAGER = CLIENT_REQUEST_MANAGERS.register(
            new ClientRequestManager().idStrict(),
            ManagedResponsePayload.TYPE,
            ManagedResponsePayload.STREAM_CODEC);
    public static final ClientResponseManager CLIENT_RESPONSE_MANAGER = CLIENT_REQUEST_MANAGERS.register(
            new ClientResponseManager().strict(),
            ManagedRequestPayload.TYPE,
            ManagedRequestPayload.STREAM_CODEC);

    public LyricoreClient(ModContainer container) {

    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Lyricore.LOGGER.info("HELLO FROM CLIENT SETUP");
        Lyricore.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    // 真注册通用 RequestManager
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        CLIENT_REQUEST_MANAGERS.register(event);
    }
}

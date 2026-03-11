package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ServerRequestManagerRegistrar extends AbstractRequestManagerRegistrar<ServerRequestManager, ServerResponseManager, ClientRequestPair> {

    public ServerRequestManagerRegistrar(String version) {
        super(version);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public ServerRequestManager register(ServerRequestManager manager) {
        rqManagers.add(manager);
        return manager;
    }

    @Override
    public ServerResponseManager register(ServerResponseManager manager) {
        rpManagers.add(manager);
        return manager;
    }

    @Override
    public void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(version);
        rqManagers.forEach(manager -> {
             registrar.playToClient(new CustomPacketPayload.Type<>(ManagedRequestPayload.nameToType(manager.name(), ManagedRequestPayload.Phase.REQUEST)),
                     ManagedRequestPayload.STREAM_CODEC, manager);
        });
        rpManagers.forEach(manager -> {
             registrar.playToServer(new CustomPacketPayload.Type<>(ManagedRequestPayload.nameToType(manager.name(), ManagedRequestPayload.Phase.RESPONSE)),
                     ManagedRequestPayload.STREAM_CODEC, manager);
        });
    }

    // 服务端启动事件
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Lyricore.LOGGER.info("[{}] 启用服务端 RequestManager。Enabling Server RequestManager.", version);
        connect();
    }

    // 服务端关闭事件
    @SubscribeEvent
    public void onServerShutdown(ServerStoppingEvent event) {
        Lyricore.LOGGER.info("[{}] 停止服务端 RequestManager。Disabling Server RequestManager.", version);
        disconnect();
    }
}

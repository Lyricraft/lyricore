package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ClientRequestManagerRegistrar extends AbstractRequestManagerRegistrar<ClientRequestManager, ClientResponseManager, ServerRequestPair> {

    public ClientRequestManagerRegistrar(String version) {
        super(version);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public ClientRequestManager register(ClientRequestManager manager) {
        rqManagers.add(manager);
        return manager;
    }

    @Override
    public ClientResponseManager register(ClientResponseManager manager) {
        rpManagers.add(manager);
        return manager;
    }

    @Override
    public void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(version);
        rqManagers.forEach(manager -> {
            registrar.playToServer(new CustomPacketPayload.Type<>(ManagedRequestPayload.nameToType(manager.name(), ManagedRequestPayload.Requester.CLIENT)),
                    ManagedRequestPayload.STREAM_CODEC, manager);
        });
        rpManagers.forEach(manager -> {
            registrar.playToClient(new CustomPacketPayload.Type<>(ManagedRequestPayload.nameToType(manager.name(), ManagedRequestPayload.Requester.SERVER)),
                    ManagedRequestPayload.STREAM_CODEC, manager);
        });
    }

    // 玩家正进入世界或服务器
    @SubscribeEvent
    public void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        Lyricore.LOGGER.info("[{}] 启用客户端 RequestManager。Enabling Client RequestManager.", version);
        connect();
    }

    // 玩家正离开世界或服务器
    @SubscribeEvent
    public void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        Lyricore.LOGGER.info("[{}] 停止客户端 RequestManager。Disabling Client RequestManager.", version);
        disconnect();
    }
}

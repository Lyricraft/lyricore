package cn.lyricraft.lyricore.network.requestManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ClientRequestManagerRegistrar extends AbstractRequestManagerRegistrar<ClientRequestManager, ClientResponseManager, ServerRequestPair> {

    public ClientRequestManagerRegistrar(String version) {
        super(version);
    }

    @Override
    public ClientRequestManager register(ClientRequestManager manager,
                                         CustomPacketPayload.Type<ManagedResponsePayload> responseType,
                                         StreamCodec<ByteBuf, ManagedResponsePayload> responseCodec) {
        rqItems.addLast(new RqItem(responseType, responseCodec, manager));
        rqManagers.addLast(manager);
        return manager;
    }

    @Override
    public ClientResponseManager register(ClientResponseManager manager,
                                          CustomPacketPayload.Type<ManagedRequestPayload> requestType,
                                          StreamCodec<ByteBuf, ManagedRequestPayload> requestCodec) {
        rpItems.addLast(new RpItem(requestType, requestCodec, manager));
        return manager;
    }

    @Override
    public void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(version);
        rqItems.forEach(item -> {
            registrar.playToServer(item.type(), item.codec(), item.manager());
        });
        rqItems = null;
        rpItems.forEach(item -> {
            registrar.playToClient(item.type(), item.codec(), item.manager());
        });
        rpItems = null;
    }
}

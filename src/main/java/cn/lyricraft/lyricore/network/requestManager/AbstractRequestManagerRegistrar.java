package cn.lyricraft.lyricore.network.requestManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRequestManagerRegistrar<Rq extends AbstractRequestManager, Rp extends AbstractResponseManager<V>, V extends AbstractRequestPair> {

    protected String version;

    protected List<AbstractRequestManagerRegistrar.RqItem> rqItems = new ArrayList<>();
    protected List<AbstractRequestManagerRegistrar.RpItem> rpItems = new ArrayList<>();

    protected List<Rq> rqManagers = new ArrayList<>();

    public AbstractRequestManagerRegistrar(String version){
        this.version = version;
    }

    public abstract Rq register(Rq manager,
                                CustomPacketPayload.Type<ManagedResponsePayload> responseType,
                                StreamCodec<ByteBuf, ManagedResponsePayload> responseCodec);

    public abstract Rp register(Rp manager,
                                CustomPacketPayload.Type<ManagedRequestPayload> requestType,
                                StreamCodec<ByteBuf, ManagedRequestPayload> requestCodec);

    public abstract void register(RegisterPayloadHandlersEvent event);

    protected record RqItem(CustomPacketPayload.Type<ManagedResponsePayload> type,
                          StreamCodec<ByteBuf, ManagedResponsePayload> codec,
                          AbstractRequestManager manager){}

    protected record RpItem(CustomPacketPayload.Type<ManagedRequestPayload> type,
                          StreamCodec<ByteBuf, ManagedRequestPayload> codec,
                          AbstractResponseManager manager){}
}

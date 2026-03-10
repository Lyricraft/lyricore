package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Function;

public class ClientResponseManager extends AbstractResponseManager<ServerRequestPair> {

    private boolean strict = false; // 在 strict 模式下，异端若发送了无法识别的请求，则直接断开连接

    public ClientResponseManager strict(){
        this.strict = true;
        return this;
    }

    public ClientResponseManager(String namespace, Function<CompoundTag, ? extends CustomPacketPayload> payload){
        super(namespace, payload);
    }

    public ClientResponseManager(){
        super();
    }

    @Override
    protected void handleRequest(CompoundTag metaNbt, IPayloadContext context){
        CompoundTag rmNbt = metaNbt.getCompound(metaNbtKey());
        if (rmNbt.isEmpty()) return;
        int id = rmNbt.getInt("id");
        ServerRequestPair pair = pairs.get(rmNbt.getString("type"));
        if (pair == null) {
            Lyricore.LOGGER.warn("服务端发送了无法识别的RequestManager请求。Server sent an unrecognized RequestManager request.\n"
                    + "请求类型 / Request Type: " + namespace + ":" + "requestManager" + " . " + rmNbt.getString("type"));
            if (strict)
                context.disconnect(Component.translatable("lyricore.multiplayer.disconnect.invalid_server_request")
                        .append("\n" + namespace + ":" + "requestManager" + " . " + rmNbt.getString("type")));
            return;
        }
        Handle handleObj = new Handle(id, pair);
        metaNbt.remove(metaNbtKey());
        pair.handleRequest(pair.bodyFromNbt(metaNbt), context, handleObj);
        if (!handleObj.isHandled()) handleObj.delay();
    }

    public class Handle extends AbstractResponseManager.Handle{

        public Handle(int id, ServerRequestPair pair) {
            super(id, pair);
        }

        @Override
        protected void send(CompoundTag nbt) {
            PacketDistributor.sendToServer(payload.apply(nbt));
        }
    }

}

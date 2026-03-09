package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Function;

public abstract class ClientResponseManager extends ResponseManager<ServerRequestPair> {
    public ClientResponseManager(String namespace, Function<CompoundTag, ? extends CustomPacketPayload> payload){
        super(namespace, payload);
    }

    public void handlerRequest(CompoundTag metaNbt, IPayloadContext context){
        CompoundTag rmNbt = metaNbt.getCompound(metaNbtKey());
        if (rmNbt.isEmpty()) return;
        int id = rmNbt.getInt("id");
        ServerRequestPair pair = pairs.get(rmNbt.getString("type"));
        if (pair == null) {
            Lyricore.LOGGER.warn("服务端发送了无法识别的RequestManager请求。Server sent an unrecognized RequestManager request.\n"
                    + "请求类型 / Request Type: " + namespace + ":" + "requestManager" + " . " + rmNbt.getString("type"));
            return;
        }
        Handle handleObj = new Handle(id, pair);
        metaNbt.remove(metaNbtKey());
        pair.handleRequest(pair.bodyFromNbt(metaNbt), context, handleObj);
        if (!handleObj.isHandled()) handleObj.delay();
    }

    public class Handle extends ResponseManager.Handle{

        public Handle(int id, ServerRequestPair pair) {
            super(id, pair);
        }

        @Override
        protected void send(CompoundTag nbt) {
            PacketDistributor.sendToServer(payload.apply(nbt));
        }
    }

}

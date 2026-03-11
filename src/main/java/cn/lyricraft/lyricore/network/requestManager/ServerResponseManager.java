package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import cn.lyricraft.lyricore.log.LogHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerResponseManager extends AbstractResponseManager<ClientRequestPair> {

    private boolean strict = false; // 在 strict 模式下，异端若发送了无法识别的请求，则直接断开连接

    public ServerResponseManager strict(){
        this.strict = true;
        return this;
    }

    public ServerResponseManager(ResourceLocation name){
        super(name);
    }

    @Override
    protected void handleRequest(CompoundTag metaNbt, IPayloadContext context){
        CompoundTag rmNbt = metaNbt.getCompound(AbstractRequestManager.META_NBT_KEY);
        if (rmNbt.isEmpty()) return;
        int id = rmNbt.getInt("id");
        ClientRequestPair pair = pairs.get(rmNbt.getString("type"));
        if (pair == null) {
            Lyricore.LOGGER.warn("玩家客户端发送的 RequestManager 请求无法识别 / Unrecognized RequestManager request from player client: "+ LogHelper.playerProfile(context.player()) +"\n"
                    + "请求类型 / Request Type: " + name.toString() + " . " + rmNbt.getString("type"));
            if (strict)
                context.disconnect(Component.translatable("lyricore.multiplayer.disconnect.invalid_request")
                        .append("\n" + name.toString() + " . " + rmNbt.getString("type")));
            return;
        }
        Handle handleObj = new Handle(id, pair, context);
        metaNbt.remove(AbstractRequestManager.META_NBT_KEY);
        pair.handleRequest(pair.bodyFromNbt(metaNbt), context, handleObj);
        if (!handleObj.isHandled()) handleObj.delay();
    }

    public class Handle extends AbstractResponseManager.Handle{
        private IPayloadContext context;

        public Handle(int id, ClientRequestPair pair, IPayloadContext context) {
            super(id, pair, ManagedRequestPayload.Requester.CLIENT);
            this.context = context;
        }

        public void disconnect(Component reason) {
            if(handled) return;
            handled = true;
            String playerProfile = LogHelper.playerProfile(context.player());
            context.disconnect(reason);
            Lyricore.LOGGER.warn("服务端在处理 RequestManager 请求时断开了与玩家的连接 / Server disconnected from the player while handling a RequestManager request: "+playerProfile+"\n"
            + "请求类型 / Request Type: " + name.toString() + " . " + pair.type());
        }

        @Override
        protected void send(CompoundTag nbt) {
            PacketDistributor.sendToPlayer((ServerPlayer) (context.player()), payload.apply(nbt));
        }
    }

}

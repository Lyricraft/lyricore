package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import cn.lyricraft.lyricore.log.LogHelper;
import cn.lyricraft.lyricore.server.ServerTypeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientRequestManager extends AbstractRequestManager<ClientRequestPair> {

    public ClientRequestManager(ResourceLocation name, int timeout, int handleExpiredInterval){
        super(name, timeout, handleExpiredInterval);
    }

    public ClientRequestManager idStrict(){
        idStrict = true;
        return this;
    }

    public ClientRequestManager timeStrict(){
        timeStrict = true;
        return this;
    }

    public boolean request(ClientRequestPair pair, ManagedRequestBody rqBody, IManagedResponseHandler handler, boolean isWaiting){
        if (!connecting || Minecraft.getInstance().level == null) return false;
        int id = random.nextInt();
        CompoundTag metaNbt = new CompoundTag();
        metaNbt.putInt("id", id);
        metaNbt.putString("type", pair.type().toString());
        metaNbt.putString("manager", name.toString());
        metaNbt.putString("requester", ManagedRequestPayload.requesterToString(ManagedRequestPayload.Requester.CLIENT));
        CompoundTag bodyNbt = rqBody.toNbt();
        bodyNbt.put(AbstractRequestManager.META_NBT_KEY, metaNbt);
        PacketDistributor.sendToServer(new ManagedRequestPayload(metaNbt));
        requests.put(id, new RequestInfo(pair, handler, System.nanoTime(), isWaiting, null));
        return true;
    }

    @Override
    protected void disconnectForTimeout(RequestInfo info) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null && !ServerTypeHelper.isInnerServer()){
            connection.disconnect(Component.translatable("lyricore.multiplayer.disconnect.server_request_timeout").
                    append("\n" + name.toString() + " . " + info.pair().type().toString()));
        }
    }

    @Override
    public void handleResponse(CompoundTag bodyNbt, IPayloadContext context){
        CompoundTag metaNBT = bodyNbt.getCompound(AbstractRequestManager.META_NBT_KEY);
        if (metaNBT.isEmpty()) return;
        int id = metaNBT.getInt("id");
        RequestInfo rqInfo = requests.get(id);
        if (rqInfo == null) {
            cannotLocateId(context);
            return;
        };
        if (metaNBT.getBoolean("reject")){
            if(rqInfo.isWaiting()) rqInfo.handler().handleResponse(rqInfo.pair().emptyResponseBody(), context, new ResponseStatus(ResponseStatus.Status.REJECTED), rqInfo);
        } else if (metaNBT.getBoolean("delay")){
            if(rqInfo.isWaiting()) rqInfo.handler().handleResponse(rqInfo.pair().emptyResponseBody(), context, new ResponseStatus(ResponseStatus.Status.DELAYED), rqInfo);
        } else {
            bodyNbt.remove(AbstractRequestManager.META_NBT_KEY);
            rqInfo.handler().handleResponse(rqInfo.pair().responseBodyFromNbt(bodyNbt), context, new ResponseStatus(ResponseStatus.Status.OK), rqInfo);
        }
        requests.remove(id);
    }

    @Override
    protected void cannotLocateId(IPayloadContext context) {
        Lyricore.LOGGER.warn("无法定位异端 RequestManager 响应的 id。Failed to locate ID of RequestManager response from ♫Otherside♫.\n"
                +"可能的玩家信息与请求类型 / Possible player info and request type: " + LogHelper.playerProfile(context.player()) + " @ " + name.toString());
        if (!ServerTypeHelper.isInnerServer()){
            context.disconnect(Component.translatable("lyricore.multiplayer.disconnect.invalid_server_response").
                    append("\n" + name.toString()));
        }
    }
}

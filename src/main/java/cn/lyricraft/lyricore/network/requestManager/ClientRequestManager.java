package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import cn.lyricraft.lyricore.log.LogHelper;
import cn.lyricraft.lyricore.server.ServerTypeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientRequestManager extends RequestManager {
    public ClientRequestManager idStrict(){
        idStrict = true;
        return this;
    }

    public ClientRequestManager timeStrict(){
        timeStrict = true;
        return this;
    }

    public boolean request(ResourceLocation type, RequestBody rqBody, IResponseHandler handler, boolean isWaiting){
        if (!connecting || Minecraft.getInstance().level == null) return false;
        int id = random.nextInt();
        CompoundTag rmNbt = new CompoundTag();
        rmNbt.putInt("id", id);
        rmNbt.putString("type", type.toString());
        CompoundTag metaNbt = rqBody.toNbt();
        metaNbt.put(metaNbtKey(), rmNbt);
        PacketDistributor.sendToServer(payload.apply(metaNbt));
        requests.put(id, new RequestInfo(type, handler, System.nanoTime() / 1_000_000, isWaiting, null));
        return true;
    }

    @Override
    protected void disconnectForTimeout(RequestInfo info) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null && !ServerTypeHelper.isInnerServer()){
            connection.disconnect(Component.translatable("lyricore.multiplayer.disconnect.server_request_timeout").
                    append("\n" + namespace + ":" + "requestManager" + " . " + info.type().toString()));
        }
    }

    @Override
    public void handleResponse(CompoundTag rpNbt, IPayloadContext context){
        CompoundTag rmNBT = rpNbt.getCompound(metaNbtKey());
        if (rmNBT.isEmpty()) return;
        int id = rmNBT.getInt("id");
        RequestInfo rqInfo = requests.get(id);
        if (rqInfo == null) {
            cannotLocateId(context);
            return;
        };
        if (rmNBT.getBoolean("reject")){
            if(rqInfo.isWaiting()) rqInfo.handler().handleResponse(new CompoundTag(), context, new ResponseStatus(ResponseStatus.Status.REJECTED), rqInfo);
        } else if (rmNBT.getBoolean("delay")){
            if(rqInfo.isWaiting()) rqInfo.handler().handleResponse(new CompoundTag(), context, new ResponseStatus(ResponseStatus.Status.DELAYED), rqInfo);
        } else {
            rpNbt.remove(metaNbtKey());
            rqInfo.handler().handleResponse(rpNbt, context, new ResponseStatus(ResponseStatus.Status.OK), rqInfo);
        }
        requests.remove(id);
    }

    @Override
    protected void cannotLocateId(IPayloadContext context) {
        Lyricore.LOGGER.warn("无法定位异端 RequestManager 响应的 id。Failed to locate ID of RequestManager response from ♫Otherside♫.\n"
                +"可能的玩家信息与请求类型 / Possible player info and request type: " + LogHelper.playerProfile(context.player()) + " @ " + namespace + ":" + "requestManager");
        if (!ServerTypeHelper.isInnerServer()){
            context.disconnect(Component.translatable("lyricore.multiplayer.disconnect.invalid_server_response").
                    append("\n" + namespace + ":" + "requestManager"));
        }
    }
}

package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import cn.lyricraft.lyricore.log.LogHelper;
import cn.lyricraft.lyricore.server.ServerTypeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ServerRequestManager extends AbstractRequestManager {

    public ServerRequestManager(String namespace, Function<CompoundTag, ? extends CustomPacketPayload> payload, int timeout, int handleExpiredInterval){
        super(namespace, payload, timeout, handleExpiredInterval);
    }

    public ServerRequestManager(){
        super();
    }

    public ServerRequestManager idStrict(){
        idStrict = true;
        return this;
    }

    public ServerRequestManager timeStrict(){
        timeStrict = true;
        return this;
    }

    public boolean request(ServerPlayer target, ResourceLocation type, Function<CompoundTag, ? extends CustomPacketPayload> payload, RequestBody rqBody, IResponseHandler handler, boolean isWaiting){
        if (!connecting) return false;
        int id = random.nextInt();
        CompoundTag metaNbt = new CompoundTag();
        metaNbt.putInt("id", id);
        metaNbt.putString("type", type.toString());
        CompoundTag rqNbt = rqBody.toNbt();
        rqNbt.put(metaNbtKey(), metaNbt);
        PacketDistributor.sendToPlayer(target, payload.apply(rqNbt));
        requests.put(id, new RequestInfo(type, handler, System.nanoTime() / 1_000_000, isWaiting, List.of(target)));
        return true;
    }

    public boolean requestToAll(ResourceLocation type, Function<CompoundTag, ? extends CustomPacketPayload> payload, RequestBody rqBody, IResponseHandler handler, boolean isWaiting){
        if (!connecting) return false;
        if (ServerLifecycleHooks.getCurrentServer() == null) return false;
        int id = random.nextInt();
        CompoundTag metaNbt = new CompoundTag();
        metaNbt.putInt("id", id);
        metaNbt.putString("type", type.toString());
        CompoundTag rqNbt = rqBody.toNbt();
        rqNbt.put(metaNbtKey(), metaNbt);
        PacketDistributor.sendToAllPlayers(payload.apply(rqNbt));
        requests.put(id, new RequestInfo(type, handler, System.nanoTime() / 1_000_000, isWaiting,
                new ArrayList<>(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())));
        return true;
    }

    @Override
    protected void disconnectForTimeout(RequestInfo info) {
        if (info.players() == null || info.players().isEmpty()) return;
        info.players().forEach(player -> {
            if (player == null || player.hasDisconnected() || ServerTypeHelper.isLocalPlayer(player)) return;
            player.connection.disconnect(Component.translatable("lyricore.multiplayer.disconnect.request_timeout").
                    append("\n" + namespace + ":" + "requestManager" + " . " + info.type().toString()));
        });
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
        if (rqInfo.players() == null){
            requests.remove(id);
            return;
        }
        for(int i = 0; i < rqInfo.players().size(); i++)
            if (rqInfo.players().get(i).getUUID() == context.player().getUUID()){
                rqInfo.players().remove(i);
                break;
            }
        if (rqInfo.players().isEmpty()) requests.remove(id);
    }

    @Override
    protected void cannotLocateId(IPayloadContext context) {
        Lyricore.LOGGER.warn("无法定位异端 RequestManager 响应的 id。Failed to locate ID of RequestManager response from ♫Otherside♫.\n"
                +"可能的玩家信息与请求类型 / Possible player info and request type: " + LogHelper.playerProfile(context.player()) + " @ " + namespace + ":" + "requestManager");
        if (idStrict && !ServerTypeHelper.isLocalPlayer(context.player())){
            context.disconnect(Component.translatable("lyricore.multiplayer.disconnect.invalid_response").
                    append("\n" + namespace + ":" + "requestManager"));
        }
    }
}

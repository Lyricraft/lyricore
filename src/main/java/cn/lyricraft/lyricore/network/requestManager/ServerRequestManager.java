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

public class ServerRequestManager extends AbstractRequestManager<ServerRequestPair> {

    public ServerRequestManager(ResourceLocation name, int timeout, int handleExpiredInterval){
        super(name, timeout, handleExpiredInterval);
    }

    public ServerRequestManager idStrict(){
        idStrict = true;
        return this;
    }

    public ServerRequestManager timeStrict(){
        timeStrict = true;
        return this;
    }

    public int request(ServerRequestPair pair, ServerPlayer target, ManagedRequestBody rqBody, IManagedResponseHandler handler, boolean isWaiting){
        if (!connecting) return -1;
        int id = random.nextInt(AbstractRequestManager.MAX_ID);
        CompoundTag metaNbt = new CompoundTag();
        metaNbt.putInt("id", id);
        metaNbt.putString("type", pair.type().toString());
        metaNbt.putString("manager", name.toString());
        CompoundTag bodyNbt = rqBody.toNbt();
        bodyNbt.put(AbstractRequestManager.META_NBT_KEY, metaNbt);
        PacketDistributor.sendToPlayer(target, new ManagedRequestPayload(bodyNbt));
        requests.put(id, new RequestInfo(pair, handler, System.nanoTime(), isWaiting, List.of(target)));
        return id;
    }

    public int requestToAll(ServerRequestPair pair, ManagedRequestBody rqBody, IManagedResponseHandler handler, boolean isWaiting){
        if (!connecting) return -1;
        if (ServerLifecycleHooks.getCurrentServer() == null) return -1;
        int id = random.nextInt(AbstractRequestManager.MAX_ID);
        CompoundTag metaNbt = new CompoundTag();
        metaNbt.putInt("id", id);
        metaNbt.putString("type", pair.type().toString());
        metaNbt.putString("manager", name.toString());
        metaNbt.putString("requester", ManagedRequestPayload.requesterToString(ManagedRequestPayload.Requester.SERVER));
        CompoundTag bodyNbt = rqBody.toNbt();
        bodyNbt.put(AbstractRequestManager.META_NBT_KEY, metaNbt);
        PacketDistributor.sendToAllPlayers(new ManagedRequestPayload(bodyNbt));
        requests.put(id, new RequestInfo(pair, handler, System.nanoTime(), isWaiting,
                new ArrayList<>(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())));
        return id;
    }

    @Override
    protected void disconnectForTimeout(RequestInfo info) {
        if (info.players() == null || info.players().isEmpty()) return;
        info.players().forEach(player -> {
            if (player == null || player.hasDisconnected() || ServerTypeHelper.isLocalPlayer(player)) return;
            player.connection.disconnect(Component.translatable("lyricore.multiplayer.disconnect.request_timeout").
                    append("\n" + name.toString() + " . " + info.pair().toString()));
        });
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
            if(rqInfo.isWaiting()) rqInfo.handler().handleResponse(rqInfo.pair().emptyResponseBody(), context, new ResponseStatus(ResponseStatus.Status.REJECTED, id), rqInfo);
        } else if (metaNBT.getBoolean("delay")){
            if(rqInfo.isWaiting()) rqInfo.handler().handleResponse(rqInfo.pair().emptyResponseBody(), context, new ResponseStatus(ResponseStatus.Status.DELAYED, id), rqInfo);
        } else {
            bodyNbt.remove(AbstractRequestManager.META_NBT_KEY);
            rqInfo.handler().handleResponse(rqInfo.pair().responseBodyFromNbt(bodyNbt), context, new ResponseStatus(ResponseStatus.Status.OK, id), rqInfo);
        }
        if (rqInfo.players() == null){
            requests.remove(id);
            return;
        }
        for(int i = 0; i < rqInfo.players().size(); i++)
            if (rqInfo.players().get(i).getUUID().equals(context.player().getUUID())){
                rqInfo.players().remove(i);
                break; // 我只删一个，然后就跳出，应该不会错乱吧。
            }
        if (rqInfo.players().isEmpty()) requests.remove(id);
    }

    @Override
    protected void cannotLocateId(IPayloadContext context) {
        Lyricore.LOGGER.warn("无法定位异端 RequestManager 响应的 id。Failed to locate ID of RequestManager response from ♫Otherside♫.\n"
                +"可能的玩家信息与请求类型 / Possible player info and request type: " + LogHelper.playerProfile(context.player()) + " @ " + name.toString());
        if (idStrict && !ServerTypeHelper.isLocalPlayer(context.player())){
            context.disconnect(Component.translatable("lyricore.multiplayer.disconnect.invalid_response").
                    append("\n" + name.toString()));
        }
    }
}

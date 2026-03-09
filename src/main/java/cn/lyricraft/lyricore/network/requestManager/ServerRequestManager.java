package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Function;

public class ServerRequestManager extends RequestManager {

    public boolean request(ServerPlayer target, ResourceLocation type, Function<CompoundTag, ? extends CustomPacketPayload> payload, RequestBody rqBody, IResponseHandler handler, boolean isWaiting){
        if (!connecting) return false;
        int id = random.nextInt();
        CompoundTag rmNbt = new CompoundTag();
        rmNbt.putInt("id", id);
        rmNbt.putString("type", type.toString());
        CompoundTag metaNbt = rqBody.toNbt();
        metaNbt.put(metaNbtKey(), rmNbt);
        PacketDistributor.sendToPlayer(target, payload.apply(metaNbt));
        requests.put(id, new RequestInfo(handler, System.nanoTime() / 1_000_000, isWaiting));
        return true;
    }
}

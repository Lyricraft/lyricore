package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientRequestManager extends RequestManager {
    public boolean request(ResourceLocation type, RequestBody rqBody, IResponseHandler handler, boolean isWaiting){
        if (!connecting || Minecraft.getInstance().level == null) return false;
        int id = random.nextInt();
        CompoundTag rmNbt = new CompoundTag();
        rmNbt.putInt("id", id);
        rmNbt.putString("type", type.toString());
        CompoundTag metaNbt = rqBody.toNbt();
        metaNbt.put(metaNbtKey(), rmNbt);
        PacketDistributor.sendToServer(payload.apply(metaNbt));
        requests.put(id, new RequestInfo(handler, System.nanoTime() / 1_000_000, isWaiting));
        return true;
    }
}

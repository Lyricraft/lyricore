package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IResponseHandler {
    void handleResponse(CompoundTag response, IPayloadContext context, AbstractRequestManager.ResponseStatus status, AbstractRequestManager.RequestInfo rqInfo);
}
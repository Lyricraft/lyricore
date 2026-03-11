package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IManagedResponseHandler {
    void handleResponse(ManagedRequestBody body, IPayloadContext context, AbstractRequestManager.ResponseStatus status, AbstractRequestManager.RequestInfo rqInfo);
}
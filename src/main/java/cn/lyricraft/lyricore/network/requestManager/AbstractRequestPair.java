package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class AbstractRequestPair<Rq extends ManagedRequestBody, Rp extends ManagedRequestBody, H extends AbstractResponseManager.Handle> {
    public abstract ResourceLocation type();
    protected abstract void handleRequest(Rq requestBody, IPayloadContext context, H handle);
    public abstract Rq requestBodyFromNbt(CompoundTag nbt);
    public abstract Rp responseBodyFromNbt(CompoundTag nbt);
    public abstract Rp emptyResponseBody();
}

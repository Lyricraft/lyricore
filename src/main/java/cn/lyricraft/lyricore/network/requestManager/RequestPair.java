package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class RequestPair<T extends RequestBody, H extends ResponseManager.Handle> {
    public abstract ResourceLocation type();
    protected abstract void handleRequest(T requestBody, IPayloadContext context, H handle);
    public abstract T bodyFromNbt(CompoundTag nbt);
}

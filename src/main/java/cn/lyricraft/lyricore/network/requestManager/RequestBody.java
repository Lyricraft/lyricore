package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;

public abstract class RequestBody {
    public abstract CompoundTag toNbt();
    protected RequestBody(CompoundTag nbt){} // To be overridden
}

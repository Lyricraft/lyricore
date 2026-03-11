package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;

public abstract class ManagedRequestBody {
    public abstract CompoundTag toNbt();
    protected ManagedRequestBody(CompoundTag nbt){} // To be overridden
}

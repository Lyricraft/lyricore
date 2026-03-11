package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;

public abstract class ManagedRequestBody {
    public abstract CompoundTag toNbt();
    public ManagedRequestBody(){} // To be overridden
    public ManagedRequestBody(CompoundTag nbt){} // To be overridden
}

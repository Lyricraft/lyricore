package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class ServerRequestPair<T extends RequestBody> extends RequestPair<T, ClientResponseManager.Handle> {}

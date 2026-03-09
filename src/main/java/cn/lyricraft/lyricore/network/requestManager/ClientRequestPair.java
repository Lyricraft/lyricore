package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class ClientRequestPair<T extends RequestBody> extends RequestPair<T, ServerResponseManager.Handle> {}

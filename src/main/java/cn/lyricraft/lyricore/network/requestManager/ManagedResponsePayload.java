package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ManagedResponsePayload(CompoundTag rpNbt) implements CustomPacketPayload {
    public static final Type<ManagedResponsePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Lyricore.MOD_NAMESPACE,
                    "managedResponsePayload"));

    public static final StreamCodec<ByteBuf, ManagedResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ManagedResponsePayload::rpNbt,
            ManagedResponsePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

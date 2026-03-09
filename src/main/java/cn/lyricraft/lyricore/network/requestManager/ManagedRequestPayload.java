package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ManagedRequestPayload(CompoundTag metaNbt) implements CustomPacketPayload {
    public static final Type<ManagedRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Lyricore.MOD_NAMESPACE,
                    "managedRequestPayload"));

    public static final StreamCodec<ByteBuf, ManagedRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ManagedRequestPayload::metaNbt,
            ManagedRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

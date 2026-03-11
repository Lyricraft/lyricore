package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ManagedRequestPayload(CompoundTag bodyNbt) implements CustomPacketPayload {

    public static final StreamCodec<ByteBuf, ManagedRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ManagedRequestPayload::bodyNbt,
            ManagedRequestPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        String path = bodyNbt().getCompound(AbstractRequestManager.META_NBT_KEY).getString("manager");
        if (path.isEmpty()) path = AbstractRequestManager.DEFAULT_NAME.toString();
        path = path.replaceFirst(":", ".");
        return new Type<ManagedRequestPayload>(ResourceLocation.fromNamespaceAndPath(Lyricore.MOD_NAMESPACE,"request_manager."+
                bodyNbt().getCompound(AbstractRequestManager.META_NBT_KEY).getString("phase") + "." + path));
    }

    public static ResourceLocation nameToType(ResourceLocation name, Phase phase){
        return ResourceLocation.fromNamespaceAndPath(Lyricore.MOD_NAMESPACE, "request_manager." +
                ManagedRequestPayload.phaseToString(phase) + "." + name.getNamespace()+ "." + name.getPath());
    }

    public enum Phase{
        REQUEST,
        RESPONSE
    }

    public static String phaseToString(Phase phase){
        switch (phase){
            case REQUEST -> {
                return "request";
            }
            case RESPONSE -> {
                return "response";
            }
        }
        return "";
    }
}
package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractResponseManager<V extends AbstractRequestPair> implements IPayloadHandler<ManagedRequestPayload> {
    protected ResourceLocation name;
    protected Function<CompoundTag, ? extends CustomPacketPayload> payload;
    protected final Map<String, V> pairs = new HashMap<>();

    public AbstractResponseManager(ResourceLocation name){
        this.name = name;
    }

    public ResourceLocation name(){
        return name;
    }

    public V registerRequestPair(V pair){
        pairs.put(pair.type().toString(), pair);
        return pair;
    }

    protected abstract void handleRequest(CompoundTag metaNbt, IPayloadContext context);

    @Override
    public void handle(ManagedRequestPayload payload, @NotNull IPayloadContext context){
        handleRequest(payload.bodyNbt(), context);
    }

    public abstract class Handle{
        protected volatile boolean handled = false;
        protected int id;
        protected V pair;

        protected ManagedRequestPayload.Requester requester;

        public Handle(int id, V pair, ManagedRequestPayload.Requester requester){
            this.id = id;
            this.pair = pair;
            this.requester = requester;
        }

        public void reject() {
            noBody("reject");
        }

        public void delay() {
            noBody("delay");
        }

        private void noBody(String how){
            if (handled) return;
            handled = true;
            CompoundTag metaNbt = newMetaNbt();
            metaNbt.putBoolean(how, true);
            CompoundTag bodyNbt = new CompoundTag();
            putMetaNbtIntoBodyNbt(bodyNbt, metaNbt);
            send(bodyNbt);
        }

        public void response(ManagedRequestBody body) {
            if (handled) return;
            handled = true;
            CompoundTag bodyNbt = body.toNbt();
            putMetaNbtIntoBodyNbt(bodyNbt, newMetaNbt());
            send(bodyNbt);
        }

        public boolean isHandled(){
            return handled;
        }

        private CompoundTag newMetaNbt(){
            CompoundTag metaNbt = new CompoundTag();
            metaNbt.putInt("id", id);
            metaNbt.putString("manager", name.toString());
            metaNbt.putString("requester", ManagedRequestPayload.requesterToString(requester));
            metaNbt.putString("phase", ManagedRequestPayload.phaseToString(ManagedRequestPayload.Phase.RESPONSE));
            return metaNbt;
        }

        private void putMetaNbtIntoBodyNbt(CompoundTag bodyNbt, CompoundTag metaNbt){
            bodyNbt.put(AbstractRequestManager.META_NBT_KEY, metaNbt);
        }

        protected abstract void send(CompoundTag nbt);
    }
}

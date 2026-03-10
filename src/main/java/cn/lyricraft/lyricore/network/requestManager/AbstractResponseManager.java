package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractResponseManager<V extends AbstractRequestPair> implements IPayloadHandler<ManagedRequestPayload> {
    protected String namespace;
    protected Function<CompoundTag, ? extends CustomPacketPayload> payload;
    protected final Map<String, V> pairs = new HashMap<>();

    public AbstractResponseManager(String namespace, Function<CompoundTag, ? extends CustomPacketPayload> payload){
        this.namespace = namespace;
        this.payload = payload;
    }

    public AbstractResponseManager(){
        this(Lyricore.MOD_NAMESPACE, ManagedResponsePayload::new);
    }

    protected String metaNbtKey(){
        return ((Objects.equals(namespace, ""))? "requestManager" : (namespace + ":" + "requestManager"));
    }

    public V registerRequestPair(V pair){
        pairs.put(pair.type().toString(), pair);
        return pair;
    }

    protected abstract void handleRequest(CompoundTag metaNbt, IPayloadContext context);

    @Override
    public void handle(ManagedRequestPayload payload, IPayloadContext context){
        handleRequest(payload.rqNbt(), context);
    }

    public abstract class Handle{
        protected volatile boolean handled = false;
        protected int id;
        protected V pair;

        public Handle(int id, V pair){
            this.id = id;
            this.pair = pair;
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

        public void response(RequestBody body) {
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
            return metaNbt;
        }

        private void putMetaNbtIntoBodyNbt(CompoundTag bodyNbt, CompoundTag metaNbt){
            bodyNbt.put(metaNbtKey(), metaNbt);
        }

        protected abstract void send(CompoundTag nbt);
    }
}

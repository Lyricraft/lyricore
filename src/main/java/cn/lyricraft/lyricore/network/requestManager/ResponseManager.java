package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class ResponseManager<V extends RequestPair> {
    protected String namespace;
    protected Function<CompoundTag, ? extends CustomPacketPayload> payload;
    protected final Map<String, V> pairs = new HashMap<>();

    public ResponseManager(String namespace, Function<CompoundTag, ? extends CustomPacketPayload> payload){
        this.namespace = namespace;
        this.payload = payload;
    }

    protected String metaNbtKey(){
        return ((Objects.equals(namespace, ""))? "requestManager" : (namespace + ":" + "requestManager"));
    }

    public V registerRequestPair(V pair){
        pairs.put(pair.type().toString(), pair);
        return pair;
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

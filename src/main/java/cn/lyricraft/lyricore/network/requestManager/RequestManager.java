package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import cn.lyricraft.lyricore.log.LogHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class RequestManager implements AutoCloseable{
    public static int DEFAULT_TIMEOUT = 30 * 1000; // 单位：毫秒
    public static int DEFAULT_HANDLE_EXPIRED_INTERVAL = 10 * 1000; // 单位：毫秒

    public boolean connecting = false;
    public String namespace;
    protected Function<CompoundTag, ? extends CustomPacketPayload> payload;
    private int timeout; // 请求过期时间，单位：毫秒
    private int handleExpiredInterval;
    private ScheduledExecutorService handleExpiredTimer = null;
    protected final Random random = new Random();


    protected Map<Integer, RequestInfo> requests = new ConcurrentHashMap<>();

    public RequestManager(String namespace, Function<CompoundTag, ? extends CustomPacketPayload> payload, int timeout, int handleExpiredInterval){
        this.namespace = namespace;
        this.payload = payload;
        this.timeout = timeout;
        this.handleExpiredInterval = handleExpiredInterval;
    }

    private void startTimer(){
        handleExpiredTimer = Executors.newSingleThreadScheduledExecutor();
        handleExpiredTimer.scheduleAtFixedRate(this::handleExpiredRequest, 0, handleExpiredInterval, TimeUnit.MILLISECONDS);
    }

    public RequestManager(){
       this(Lyricore.MOD_NAMESPACE, ManagedRequestPayload::new, DEFAULT_TIMEOUT, DEFAULT_HANDLE_EXPIRED_INTERVAL);
    }

    @Override
    public void close(){
        // connecting = false; // 没有必要
        if (handleExpiredTimer != null && !handleExpiredTimer.isShutdown()) handleExpiredTimer.shutdown();
        // handleExpiredTimer = null; // 没有必要
        requests.clear();
    }

    public void connect(){
        if (handleExpiredTimer != null && !handleExpiredTimer.isShutdown()) handleExpiredTimer.shutdown();
        requests.clear();
        startTimer();
        connecting = true;
    }

    public void disconnect(){
        connecting = false;
        if (handleExpiredTimer != null && !handleExpiredTimer.isShutdown()) handleExpiredTimer.shutdown();
        handleExpiredTimer = null;
        requests.forEach((id,info) -> {
            if (info.isWaiting())
                info.handler().handleResponse(new CompoundTag(), null, new ResponseStatus(ResponseStatus.Status.DISCONNECT));
        });
        requests.clear();
    }

    private void handleExpiredRequest(){
        long time = System.nanoTime() / 1_000_000;
        List<Integer> toRemove = new ArrayList<>();
        requests.forEach((id,info) -> {
            if (time - info.requestTime() > timeout){
                if (info.isWaiting())
                    info.handler().handleResponse(new CompoundTag(), null, new ResponseStatus(ResponseStatus.Status.TIMEOUT));
                toRemove.addLast(id);
            }
        });
        toRemove.forEach(id -> {
            requests.remove(id);
        });
    }

    private void handleResponse(CompoundTag rpNbt, IPayloadContext context){
        CompoundTag rmNBT = rpNbt.getCompound(metaNbtKey());
        if (rmNBT.isEmpty()) return;
        int id = rmNBT.getInt("id");
        RequestInfo rqInfo = requests.get(id);
        if (rqInfo == null) {
            Lyricore.LOGGER.warn("无法定位异端 RequestManager 响应的 id。Failed to locate ID of RequestManager response from ♫Otherside♫.\n"
            +"可能的玩家信息与请求类型 / Possible player info and request type: " + LogHelper.playerProfile(context.player()) + " @ " + namespace + ":" + "requestManager" + " . " + rpNbt.getString("type"));
            return;
        };
        if (rmNBT.getBoolean("reject")){
            if(rqInfo.isWaiting()) rqInfo.handler.handleResponse(new CompoundTag(), context, new ResponseStatus(ResponseStatus.Status.REJECTED));
        } else if (rmNBT.getBoolean("delay")){
            if(rqInfo.isWaiting()) rqInfo.handler.handleResponse(new CompoundTag(), context, new ResponseStatus(ResponseStatus.Status.DELAYED));
        } else {
            rpNbt.remove(metaNbtKey());
            rqInfo.handler.handleResponse(rpNbt, context, new ResponseStatus(ResponseStatus.Status.OK));
        }
        requests.remove(id);
    }

    protected String metaNbtKey(){
        return ((Objects.equals(namespace, ""))? "requestManager" : (namespace + ":" + "requestManager"));
    }

    protected record RequestInfo(IResponseHandler handler, long requestTime, boolean isWaiting){}

    public record ResponseStatus(Status status){
        public enum Status{
            OK,
            UNKNOWN_ERROR,
            TIMEOUT, // 在规定时间内未收到答复
            DISCONNECT, // 你退了
            REJECTED, // 被对方拒绝了
            DELAYED, // 对方走完了处理函数，也没回答
            PLAYER_LEFT // 服务端对其发送的客户端退了
        }

        public boolean success(){
            return (status == Status.OK);
        }

        public boolean isConnecting(){
            return (status != Status.DISCONNECT);
        }
    }
}

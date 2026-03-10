package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class AbstractRequestManager implements AutoCloseable, IPayloadHandler<ManagedResponsePayload> {
    public static int DEFAULT_TIMEOUT = 30 * 1000; // 单位：毫秒
    public static int DEFAULT_HANDLE_EXPIRED_INTERVAL = 10 * 1000; // 单位：毫秒

    protected boolean idStrict = false; // 在 idStrict 模式下，异端若发送了无法识别的相应，则直接断开连接
    protected boolean timeStrict = false; // 在 timsStrict 模式下，异端若未在要求时间内相应，则直接断开连接

    public boolean connecting = false;
    public String namespace;
    protected Function<CompoundTag, ? extends CustomPacketPayload> payload;
    private int timeout; // 请求过期时间，单位：毫秒
    private int handleExpiredInterval;
    private ScheduledExecutorService handleExpiredTimer = null;
    protected final Random random = new Random();


    protected Map<Integer, RequestInfo> requests = new ConcurrentHashMap<>();

    public AbstractRequestManager(String namespace, Function<CompoundTag, ? extends CustomPacketPayload> payload, int timeout, int handleExpiredInterval){
        this.namespace = namespace;
        this.payload = payload;
        this.timeout = timeout;
        this.handleExpiredInterval = handleExpiredInterval;
    }

    private void startTimer(){
        handleExpiredTimer = Executors.newSingleThreadScheduledExecutor();
        handleExpiredTimer.scheduleAtFixedRate(this::handleExpiredRequest, 0, handleExpiredInterval, TimeUnit.MILLISECONDS);
    }

    public AbstractRequestManager(){
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
                info.handler().handleResponse(new CompoundTag(), null, new ResponseStatus(ResponseStatus.Status.DISCONNECT), info);
        });
        requests.clear();
    }

    private void handleExpiredRequest(){
        long time = System.nanoTime() / 1_000_000;
        List<Integer> toRemove = new ArrayList<>();
        requests.forEach((id,info) -> {
            if (time - info.requestTime() > timeout){
                if (timeStrict)
                    disconnectForTimeout(info);
                else if (info.isWaiting())
                    info.handler().handleResponse(new CompoundTag(), null, new ResponseStatus(ResponseStatus.Status.TIMEOUT), info);
                toRemove.addLast(id);
            }
        });
        toRemove.forEach(id -> {
            requests.remove(id);
        });
    }

    protected abstract void disconnectForTimeout(RequestInfo info);

    @Override
    public void handle(ManagedResponsePayload payload, IPayloadContext context){
        handleResponse(payload.rpNbt(), context);
    }

    public abstract void handleResponse(CompoundTag rpNbt, IPayloadContext context);

    protected abstract void cannotLocateId(IPayloadContext context);

    protected String metaNbtKey(){
        return ((Objects.equals(namespace, ""))? "requestManager" : (namespace + ":" + "requestManager"));
    }

    public record RequestInfo(ResourceLocation type, IResponseHandler handler, long requestTime, boolean isWaiting, List<ServerPlayer> players){}

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

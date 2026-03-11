package cn.lyricraft.lyricore.network.requestManager;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRequestManager<P extends AbstractRequestPair> implements AutoCloseable, IPayloadHandler<ManagedRequestPayload> {
    public static int DEFAULT_TIMEOUT = 30 * 1000; // 单位：毫秒
    public static int DEFAULT_HANDLE_EXPIRED_INTERVAL = 10 * 1000; // 单位：毫秒
    public static int MAX_ID = 2147483647; // 最大ID
    public static String META_NBT_KEY = Lyricore.MOD_NAMESPACE + ":requestManager";
    public static ResourceLocation DEFAULT_NAME = ResourceLocation.fromNamespaceAndPath(Lyricore.MOD_NAMESPACE, "default");

    protected boolean idStrict = false; // 在 idStrict 模式下，异端若发送了无法识别的相应，则直接断开连接
    protected boolean timeStrict = false; // 在 timeStrict 模式下，异端若未在要求时间内相应，则直接断开连接

    public boolean connecting = false;
    public ResourceLocation name;
    private long timeout; // 请求过期时间。单位：纳秒
    private int handleExpiredInterval; // 清理超时事件间隔。单位：毫秒
    private ScheduledExecutorService handleExpiredTimer = null;
    protected final Random random = new Random();


    protected Map<Integer, RequestInfo> requests = new ConcurrentHashMap<>();

    public AbstractRequestManager(ResourceLocation name, int timeout, int handleExpiredInterval){
        this.name = name;
        this.timeout = timeout * 1_000_000L; // 转为纳秒
        this.handleExpiredInterval = handleExpiredInterval;
    }

    public ResourceLocation name(){
        return name;
    }

    private void startTimer(){
        handleExpiredTimer = Executors.newSingleThreadScheduledExecutor();
        handleExpiredTimer.scheduleAtFixedRate(this::handleExpiredRequest, 0, handleExpiredInterval, TimeUnit.MILLISECONDS);
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
                info.handler().handleResponse(info.pair().emptyResponseBody(), null, new ResponseStatus(ResponseStatus.Status.DISCONNECT, id), info);
        });
        requests.clear();
    }

    private void handleExpiredRequest(){
        long time = System.nanoTime();
        List<Integer> toRemove = new ArrayList<>();
        requests.forEach((id,info) -> {
            if (time - info.requestTime() > timeout){
                if (timeStrict)
                    disconnectForTimeout(info);
                else if (info.isWaiting())
                    info.handler().handleResponse(info.pair().emptyResponseBody(), null, new ResponseStatus(ResponseStatus.Status.TIMEOUT, id), info);
                toRemove.add(id);
            }
        });
        toRemove.forEach(id -> {
            requests.remove(id);
        });
    }

    protected abstract void disconnectForTimeout(RequestInfo info);

    @Override
    public void handle(ManagedRequestPayload payload, @NotNull IPayloadContext context){
        handleResponse(payload.bodyNbt(), context);
    }

    public abstract void handleResponse(CompoundTag rpNbt, IPayloadContext context);

    protected abstract void cannotLocateId(IPayloadContext context);

    public record RequestInfo(AbstractRequestPair pair, IManagedResponseHandler handler, long requestTime, boolean isWaiting, List<ServerPlayer> players){}

    public record ResponseStatus(Status status, int id){
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

package cn.lyricraft.lyricore.network.requestManager;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRequestManagerRegistrar<Rq extends AbstractRequestManager, Rp extends AbstractResponseManager<V>, V extends AbstractRequestPair> {

    protected String version;

    protected List<Rq> rqManagers = new ArrayList<>();
    protected List<Rp> rpManagers = new ArrayList<>();

    public AbstractRequestManagerRegistrar(String version){
        this.version = version;
    }

    public abstract Rq register(Rq manager);

    public abstract Rp register(Rp manager);

    public abstract void register(RegisterPayloadHandlersEvent event);

    public void connect(){
        rqManagers.forEach(AbstractRequestManager::connect);
    }

    public void disconnect(){
        rqManagers.forEach(AbstractRequestManager::disconnect);
    }
}

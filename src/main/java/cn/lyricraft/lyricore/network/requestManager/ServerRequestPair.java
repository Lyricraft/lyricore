package cn.lyricraft.lyricore.network.requestManager;

public abstract class ServerRequestPair<Rq extends ManagedRequestBody, Rp extends ManagedRequestBody> extends AbstractRequestPair<Rq, Rp, ClientResponseManager.Handle> {}

package cn.lyricraft.lyricore.network.requestManager;

public abstract class ClientRequestPair<Rq extends ManagedRequestBody, Rp extends ManagedRequestBody> extends AbstractRequestPair<Rq, Rp, ServerResponseManager.Handle> {}

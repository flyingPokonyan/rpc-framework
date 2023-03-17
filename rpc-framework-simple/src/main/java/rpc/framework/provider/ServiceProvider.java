package rpc.framework.provider;

import rpc.framework.config.RpcServiceConfig;

public interface ServiceProvider {

    /**
     *
     * @param rpcServiceConfig
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String serviceName);

    /**
     * 服务上线
     * @param rpcServiceConfig
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}

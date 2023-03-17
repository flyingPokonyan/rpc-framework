package rpc.framework.provider.impl;

import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.extension.ExtensionLoader;
import rpc.framework.config.RpcServiceConfig;
import rpc.framework.provider.ServiceProvider;
import rpc.framework.register.ServiceRegister;
import rpc.framework.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    private final Map<String,Object> serviceMap;

    private final Set<Object> registeredService;

    private final ServiceRegister serviceRegister;

    public ZkServiceProviderImpl() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegister = ExtensionLoader.getExtensionLoader(ServiceRegister.class).getExtension("zk");
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(serviceMap.get(rpcServiceName) != null){
            return;
        }
        registeredService.add(rpcServiceConfig.getService());
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());

    }

    @Override
    public Object getService(String serviceName) {
        Object o = serviceMap.get(serviceName);
        if(o == null){
            throw new RuntimeException("未找到服务");
        }
        return o;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegister.registerService(rpcServiceConfig.getRpcServiceName(),new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (Exception e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}

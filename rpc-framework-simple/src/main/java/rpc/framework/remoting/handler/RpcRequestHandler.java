package rpc.framework.remoting.handler;

import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.provider.ServiceProvider;
import rpc.framework.provider.impl.ZkServiceProviderImpl;
import rpc.framework.remoting.dto.RpcRequest;

import java.lang.reflect.Method;

@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler(){
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest,service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest,Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service,rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
}

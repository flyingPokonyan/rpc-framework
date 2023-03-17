package rpc.framework.proxy;

import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.enums.RpcErrorMessageEnum;
import rpc.framework.common.enums.RpcResponseCodeEnum;
import rpc.framework.common.exception.RpcException;
import rpc.framework.config.RpcServiceConfig;
import rpc.framework.remoting.dto.RpcRequest;
import rpc.framework.remoting.dto.RpcResponse;
import rpc.framework.remoting.transport.RpcRequestTransport;
import rpc.framework.remoting.transport.netty.client.NettyRpcClient;
import rpc.framework.remoting.transport.socket.RpcSocketClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";


    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public <T> T getProxy(Class<T> clazz){
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .interfaceName(method.getDeclaringClass().getName())
                .requestId(UUID.randomUUID().toString())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if(rpcRequestTransport instanceof NettyRpcClient){
            CompletableFuture<RpcResponse<Object>> future = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = future.get();
        }else if (rpcRequestTransport instanceof RpcSocketClient){
            rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        }



        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}

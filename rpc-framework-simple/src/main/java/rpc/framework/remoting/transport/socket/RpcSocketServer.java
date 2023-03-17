package rpc.framework.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.common.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import rpc.framework.config.CustomShutdownHook;
import rpc.framework.config.RpcServiceConfig;
import rpc.framework.provider.ServiceProvider;
import rpc.framework.provider.impl.ZkServiceProviderImpl;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


@Slf4j
public class RpcSocketServer{

    private final ServiceProvider serviceProvider;
    private final ExecutorService threadPool;

    public RpcSocketServer() {
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){
        try(ServerSocket serverSocket = new ServerSocket()){
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            serverSocket.bind(new InetSocketAddress(hostAddress,9998));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = serverSocket.accept()) != null){
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        }catch (Exception e){
            log.error("occur IOException:", e);
        }
    }
}

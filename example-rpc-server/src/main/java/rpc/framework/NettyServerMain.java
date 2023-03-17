package rpc.framework;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import rpc.framework.annotation.RpcScan;
import rpc.framework.remoting.transport.netty.server.NettyRpcServer;


@RpcScan(basePackage = {"rpc.framework"})
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        nettyRpcServer.start();

    }
}

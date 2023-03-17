package rpc.framework.remoting.transport.netty.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testng.annotations.Test;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.common.utils.RuntimeUtil;
import rpc.framework.common.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import rpc.framework.config.CustomShutdownHook;
import rpc.framework.config.RpcServiceConfig;
import rpc.framework.provider.ServiceProvider;
import rpc.framework.provider.impl.ZkServiceProviderImpl;
import rpc.framework.register.ServiceDiscovery;
import rpc.framework.remoting.transport.netty.codec.RpcMessageDecoder;
import rpc.framework.remoting.transport.netty.codec.RpcMessageEncoder;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component("nettyRpcServer")
public class NettyRpcServer {

    public static final int PORT = 8888;
    
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    /**
     * 注册服务
     * @param rpcServiceConfig
     */
    public void register(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        DefaultEventLoopGroup serviceHandleGroup = new DefaultEventLoopGroup(
                RuntimeUtil.cpus()*2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group",false)
        );

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(workerGroup,bossGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(serviceHandleGroup,new NettyRpcServerHandler());
                        }
                    });
            // 绑定端口，同步等待绑定成功
            ChannelFuture future = bootstrap.bind(NettyRpcServer.PORT).sync();
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();

        }catch (Exception e){
            log.error("occur exception when start server:", e);
        }finally {
            log.error("shutdown bossGroup and workerGroup");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            serviceHandleGroup.shutdownGracefully();
        }
    }

//    @Test
//    public void test() throws UnknownHostException {
//        System.out.println(InetAddress.getLocalHost().getHostAddress());
//    }

    @Test
    public void test(){
        System.out.println(ServiceDiscovery.class.getName());
    }
}

package rpc.framework.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.enums.CompressTypeEnum;
import rpc.framework.common.enums.SerializationTypeEnum;
import rpc.framework.common.extension.ExtensionLoader;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.register.ServiceDiscovery;
import rpc.framework.remoting.dto.RpcMessage;
import rpc.framework.remoting.dto.RpcRequest;
import rpc.framework.remoting.dto.RpcResponse;
import rpc.framework.remoting.transport.RpcRequestTransport;
import rpc.framework.remoting.transport.constants.RpcConstants;
import rpc.framework.remoting.transport.netty.codec.RpcMessageDecoder;
import rpc.framework.remoting.transport.netty.codec.RpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    private final ServiceDiscovery serviceDiscovery;
    private final ChannelProvider channelProvider;
    private final UnprocessedRequests unprocessedRequests;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient(){
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,15,0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });

        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }


    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        System.out.println("doConnect:===========" + inetSocketAddress);
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }


    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //存放返回值，异步获取
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);

        Channel channel = getChannel(inetSocketAddress);
        if(channel.isActive()){
            //存放未处理的rpc请求
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().messageType(RpcConstants.REQUEST_TYPE)
                    .codec(SerializationTypeEnum.KRYO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .data(rpcRequest)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener)future -> {
                if(future.isSuccess()){
                    log.info("client send message: [{}]", rpcMessage);
                }else{
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });

        }else {
            throw new IllegalStateException();
        }


        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel == null){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress,channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}

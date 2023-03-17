package rpc.framework.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.enums.CompressTypeEnum;
import rpc.framework.common.enums.SerializationTypeEnum;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.remoting.dto.RpcMessage;
import rpc.framework.remoting.dto.RpcResponse;
import rpc.framework.remoting.transport.constants.RpcConstants;

import java.net.InetSocketAddress;


@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler(){
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg: [{}]", msg);
            if(msg instanceof RpcMessage){
                RpcMessage rpcMessage = (RpcMessage) msg;
                if(rpcMessage.getMessageType() == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heart [{}]", rpcMessage.getData());
                }else {
                    RpcResponse rpcResponse = (RpcResponse) rpcMessage.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }

        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    //客户端15秒内没有数据发送到服务器端，会触发此事件，向服务端发送心跳消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            //拿到客户端的channel发送心跳消息
            Channel channel = nettyRpcClient.getChannel((InetSocketAddress)ctx.channel().remoteAddress());
            RpcMessage message = new RpcMessage();
            message.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
            message.setCompress(CompressTypeEnum.GZIP.getCode());
            message.setCodec(SerializationTypeEnum.KRYO.getCode());
            message.setData(RpcConstants.ping);
            channel.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

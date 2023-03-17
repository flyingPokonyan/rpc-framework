package rpc.framework.remoting.transport.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.enums.CompressTypeEnum;
import rpc.framework.common.enums.RpcResponseCodeEnum;
import rpc.framework.common.enums.SerializationTypeEnum;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.remoting.dto.RpcMessage;
import rpc.framework.remoting.dto.RpcRequest;
import rpc.framework.remoting.dto.RpcResponse;
import rpc.framework.remoting.handler.RpcRequestHandler;
import rpc.framework.remoting.transport.constants.RpcConstants;


@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try {
            if(msg instanceof RpcMessage){
                byte messageType = ((RpcMessage) msg).getMessageType();
                log.info("server receive msg: [{}] ", msg);
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                //
                if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.pong);
                }else{
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if(ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> success = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(success);
                    }else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }

                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.READER_IDLE){
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}

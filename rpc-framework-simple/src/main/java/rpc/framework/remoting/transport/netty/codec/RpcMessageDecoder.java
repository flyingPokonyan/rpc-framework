package rpc.framework.remoting.transport.netty.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.enums.CompressTypeEnum;
import rpc.framework.common.enums.SerializationTypeEnum;
import rpc.framework.common.extension.ExtensionLoader;
import rpc.framework.compress.Compress;
import rpc.framework.remoting.dto.RpcMessage;
import rpc.framework.remoting.dto.RpcRequest;
import rpc.framework.remoting.dto.RpcResponse;
import rpc.framework.remoting.transport.constants.RpcConstants;
import rpc.framework.serialize.Serializer;

import java.util.Arrays;

/**
 * 对接收到的数据进行指定格式的解码
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B codec（序列化类型）      1B compress（压缩类型）4B  requestId（请求的Id）
 * body（object类型数据）
 */

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder(){
        super(RpcConstants.MAX_FRAME_LENGTH,5,4,-9,0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decode;
            if(frame.readableBytes() >= RpcConstants.TOTAL_LENGTH){
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    e.printStackTrace();
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf frame) {
        checkMagicNum(frame);
        checkVision(frame);
        int fullLength = frame.readInt();
        byte messageType = frame.readByte();
        byte codecType = frame.readByte();
        byte compressType = frame.readByte();
        int requestId = frame.readInt();
        RpcMessage rpcMessage = RpcMessage.builder().messageType(messageType)
                .codec(codecType)
                .requestId(requestId)
                .compress(compressType)
                .build();
        if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.ping);
            return rpcMessage;
        }
        if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.pong);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if(bodyLength > 0){
            byte[] bs = new byte[bodyLength];
            frame.readBytes(bs);
            //解压缩
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            byte[] decompress = compress.decompress(bs);

            //反序列化
            String serializeName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializeName);
            if(messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest deserialize = serializer.deserialize(decompress, RpcRequest.class);
                rpcMessage.setData(deserialize);
            }else{
                RpcResponse deserialize = serializer.deserialize(decompress, RpcResponse.class);
                rpcMessage.setData(deserialize);
            }

        }
        return rpcMessage;
    }


    private void checkVision(ByteBuf in){
        byte version = in.readByte();
        if(version != RpcConstants.VERSION){
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNum(ByteBuf in){
        int length = RpcConstants.MAGIC_NUMBER.length;
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        for(int i = 0; i < bytes.length; i++){
            if(bytes[i] != RpcConstants.MAGIC_NUMBER[i]){
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(bytes));
            }
        }
    }

}

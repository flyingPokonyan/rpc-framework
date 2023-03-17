package rpc.framework.remoting.transport.netty.codec;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import rpc.framework.common.enums.CompressTypeEnum;
import rpc.framework.common.enums.SerializationTypeEnum;
import rpc.framework.common.extension.ExtensionLoader;
import rpc.framework.compress.Compress;
import rpc.framework.remoting.dto.RpcMessage;
import rpc.framework.remoting.transport.constants.RpcConstants;
import rpc.framework.serialize.Serializer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义协议 用于对Rpc发送和接收的数据进行编码
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B codec（序列化类型）      1B compress（压缩类型）4B  requestId（请求的Id）
 * body（object类型数据）
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf buf) throws Exception {
        try {
            buf.writeBytes(RpcConstants.MAGIC_NUMBER);
            buf.writeByte(RpcConstants.VERSION);
            //留一个地方用来写本次发送的数据长度 head length + data length
            buf.writerIndex(buf.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            buf.writeByte(messageType);
            buf.writeByte(rpcMessage.getCodec());
            buf.writeByte(CompressTypeEnum.GZIP.getCode());
            buf.writeInt(ATOMIC_INTEGER.getAndIncrement());     //requestId
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            if(messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                //序列化
                String SerializerName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(SerializerName);
                byte[] data = serializer.serialize(rpcMessage.getData());

                //压缩
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.compress(data);
                fullLength += bodyBytes.length;

            }
            if(bodyBytes != null){
                buf.writeBytes(bodyBytes);
            }
            int writerIndex = buf.writerIndex();
            buf.writerIndex(writerIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            buf.writeInt(fullLength);
            buf.writerIndex(writerIndex);
        }catch (Exception e){
            log.error("编码错误");
            throw new RuntimeException("编码错误");
        }
    }



    @Test
    public void test(){
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(new byte[]{'a','b','c'});
        buffer.writerIndex(5);
        buffer.writeByte('d');
        buffer.writeByte('e');
        buffer.writeByte('f');
        System.out.println(buffer.toString(StandardCharsets.UTF_8));

    }
}

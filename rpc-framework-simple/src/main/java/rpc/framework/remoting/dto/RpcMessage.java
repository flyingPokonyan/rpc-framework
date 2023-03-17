package rpc.framework.remoting.dto;


import lombok.*;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class RpcMessage {

    /**
     * 消息类型
     */
    private byte messageType;

    /**
     * 序列化方式
     */
    private byte codec;

    /**
     * 压缩方式
     */
    private byte compress;

    /**
     * request id
     */
    private int requestId;

    /**
     * request data
     */
    private Object data;
}

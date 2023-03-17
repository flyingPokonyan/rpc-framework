package rpc.framework.remoting.dto;


import lombok.*;
import rpc.framework.common.enums.RpcResponseCodeEnum;

import java.io.Serializable;

/**
 * 封装服务端返回时发送的数据
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 1905122042311950257L;

    private String requestId;

    private Integer code;

    private String message;

    //返回的数据
    private T data;

    //调用成功
    public static <T> RpcResponse<T> success(T data,String requestId){
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        rpcResponse.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        rpcResponse.setRequestId(requestId);
        if(data != null){
            rpcResponse.setData(data);
        }
        return rpcResponse;
    }

    //调用失败
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }

}

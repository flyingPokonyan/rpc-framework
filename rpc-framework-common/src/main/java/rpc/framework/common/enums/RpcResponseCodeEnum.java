package rpc.framework.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum RpcResponseCodeEnum {

    SUCCESS(200,"RPC调用成功！"),
    FAIL(500,"RPC调用失败");


    private final Integer code;
    private final String message;
}

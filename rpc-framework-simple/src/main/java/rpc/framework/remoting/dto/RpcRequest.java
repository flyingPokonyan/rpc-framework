package rpc.framework.remoting.dto;


import lombok.*;

import java.io.Serializable;

/**
 * 封装客户端远程调用发送请求的类
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950257L;

    private String requestId;

    private String interfaceName;

    private String methodName;

    private Object[] parameters;

    private Class<?>[] paramTypes;

    private String group;

    private String version;

    public String getRpcServiceName(){
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }

}

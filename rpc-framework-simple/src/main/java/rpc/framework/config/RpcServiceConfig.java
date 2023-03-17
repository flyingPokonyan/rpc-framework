package rpc.framework.config;


import lombok.*;
import org.testng.annotations.Test;
import rpc.framework.register.zookeeper.ZkServiceRegisterImpl;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RpcServiceConfig {

    private String group = "";


    private String version = "";

    private Object service;

    public String getRpcServiceName(){
        return getServiceName() + this.getGroup() + this.getVersion();
    }

    private String getServiceName(){

        return service.getClass().getInterfaces()[0].getCanonicalName();
    }


}

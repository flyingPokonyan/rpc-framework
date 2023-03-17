package rpc.framework.register;


import rpc.framework.common.extension.SPI;
import rpc.framework.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {

    /**
     * 用于客户端发现服务
     * @param rpcRequest    rpc请求实体类
     * @return              远程方法调用返回值
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}

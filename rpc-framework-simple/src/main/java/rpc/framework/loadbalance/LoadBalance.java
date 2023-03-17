package rpc.framework.loadbalance;

import rpc.framework.common.extension.SPI;
import rpc.framework.remoting.dto.RpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {

    /**
     * 从已知的服务列表中选择一个服务 返回服务地址 （负载均衡）
     * @param serviceUrlList
     * @param rpcRequest
     * @return
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}

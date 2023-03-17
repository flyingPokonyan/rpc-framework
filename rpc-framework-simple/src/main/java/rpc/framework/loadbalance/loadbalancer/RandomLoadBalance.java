package rpc.framework.loadbalance.loadbalancer;

import rpc.framework.loadbalance.AbstractLoadBalance;
import rpc.framework.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {

    /**
     * 随机负载均衡
     * @param serviceUrlList
     * @param rpcRequest
     * @return
     */
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        return serviceUrlList.get(new Random().nextInt(serviceUrlList.size()));
    }



}

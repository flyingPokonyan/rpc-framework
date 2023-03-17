package rpc.framework.register.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import rpc.framework.common.extension.ExtensionLoader;
import rpc.framework.loadbalance.LoadBalance;
import rpc.framework.register.ServiceDiscovery;
import rpc.framework.register.zookeeper.util.CuratorUtils;
import rpc.framework.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * zk实现服务发现
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(childrenNodes.isEmpty()){
            throw new RuntimeException("rpc服务未找到");
        }
        //负载均衡
        String serviceAddress = loadBalance.selectServiceAddress(childrenNodes, rpcRequest);
        log.info("通过负载均衡算法处理过返回的服务地址为::{}",serviceAddress);
        String[] split = serviceAddress.split(":");

        return new InetSocketAddress(split[0],Integer.parseInt(split[1]));

    }
}

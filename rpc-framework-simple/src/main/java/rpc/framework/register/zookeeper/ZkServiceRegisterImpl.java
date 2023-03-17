package rpc.framework.register.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import rpc.framework.register.ServiceRegister;
import rpc.framework.register.zookeeper.util.CuratorUtils;

import java.net.InetSocketAddress;


/**
 * zk实现服务注册
 */
public class ZkServiceRegisterImpl implements ServiceRegister {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(servicePath,zkClient);
    }
}

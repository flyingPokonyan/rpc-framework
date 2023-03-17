package rpc.framework.register.zookeeper.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import rpc.framework.common.enums.RpcConfigEnum;
import rpc.framework.common.utils.PropertiesFileUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper客户端工具类
 */
@AllArgsConstructor
@Data
@Slf4j
public class CuratorUtils {

    //重试间隔睡眠时间
    private static final int BASE_SLEEP_TIME = 1000;

    //最大重试次数
    private static final int MAX_RETRIES = 3;

    //zookeeper注册中心 注册服务的根节点路径
    public static final String ZK_REGISTER_PATH = "/rpc-fw";

    private static CuratorFramework zkClient;

    private static final String DEFAULT_ZK_ADDRESS = "192.168.198.164:2181";

    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

    private static final Set<String> REGISTER_PATH_SET = ConcurrentHashMap.newKeySet();


    /**
     * 注册持久节点   当zookeeper断开连接后不会被删除
     * @param path
     * @param zkClient
     */
    public static void createPersistentNode(String path,CuratorFramework zkClient){
        try {
            if(REGISTER_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("The node is already exists,The node is :[{}]",path);
            }else{
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node create successfully,The node is :[{}]",path);
            }
            REGISTER_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }


    }

    /**
     * Gets the children under a node
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version1
     * @return All child nodes under the specified node
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * Empty the registry of data
     * 服务下线
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTER_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if(p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTER_PATH_SET.toString());
    }


    public static CuratorFramework getZkClient() {
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zkAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZK_ADDRESS;

        //判断当前zkClient是否存在
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }
        //失败重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();

        try {
            //30后未连接成功，抛出异常
            if(!zkClient.blockUntilConnected(30,TimeUnit.SECONDS)){
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }


    /**
     * Registers to listen for changes to the specified node
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version
     *
     *
     * PathChildrenCache 监听指定路径下孩子节点的创建，删除、以及节点数据的更新
     *                       监听的目的是当服务发生变化时要及时感知并通知给客户端
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_PATH + "/" + rpcServiceName;

        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient,servicePath,true);
        PathChildrenCacheListener listener = (curatorFramework,event) ->{
            List<String> path = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(servicePath,path);
        };
        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCache.start();
    }


}

package rpc.framework.loadbalance.loadbalancer;

import org.testng.annotations.Test;
import rpc.framework.loadbalance.AbstractLoadBalance;
import rpc.framework.remoting.dto.RpcRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private AtomicInteger count = new AtomicInteger(0);

    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {

        return serviceUrlList.get(count.getAndIncrement() % serviceUrlList.size());
    }

    @Test
    public void test() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        List<String> list = new ArrayList<>();
        list.add("AAA");
        list.add("BBB");
        list.add("CCC");
        list.add("DDD");
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + ":" + list.get(count.getAndIncrement() % list.size()));
            }
        }).start();
        for (int i = 0; i < 20; i++) {
            Thread.sleep(1);
            System.out.println(Thread.currentThread().getName() + ":" + list.get(count.getAndIncrement() % list.size()));
        }

    }
}

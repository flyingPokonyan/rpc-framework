package rpc.framework;

import lombok.extern.slf4j.Slf4j;
import rpc.Hello;
import rpc.HelloService;
import rpc.framework.annotation.RpcService;


@Slf4j
@RpcService(group = "defaultGroup",version = "1.0")
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl 1被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}

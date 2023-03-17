package rpc.framework.controller;

import org.springframework.stereotype.Component;
import rpc.Hello;
import rpc.HelloService;
import rpc.framework.annotation.RpcReference;


@Component("helloController")
public class HelloController {

    @RpcReference(group = "defaultGroup",version = "1.0")
    HelloService helloService;

    public void run(){
        String hello = helloService.hello(new Hello("你好", "这是rpc"));
        System.out.println(hello);
    }
}

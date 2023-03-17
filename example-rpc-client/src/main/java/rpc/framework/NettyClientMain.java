package rpc.framework;

import rpc.framework.annotation.RpcScan;
import rpc.framework.controller.HelloController;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;



@RpcScan(basePackage = {"rpc.framework"})
public class NettyClientMain {


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ioc = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController)ioc.getBean("helloController");
        helloController.run();
    }
}

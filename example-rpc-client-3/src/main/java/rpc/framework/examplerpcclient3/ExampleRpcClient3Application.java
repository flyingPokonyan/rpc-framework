package rpc.framework.examplerpcclient3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import rpc.framework.annotation.RpcScan;


@SpringBootApplication
@RpcScan(basePackage = "rpc.framework")
public class ExampleRpcClient3Application {

    public static void main(String[] args) {
        SpringApplication.run(ExampleRpcClient3Application.class, args);
    }

}

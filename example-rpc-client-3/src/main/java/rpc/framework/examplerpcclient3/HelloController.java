package rpc.framework.examplerpcclient3;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rpc.Hello;
import rpc.HelloService;
import rpc.framework.annotation.RpcReference;

@Controller
public class HelloController {

    @RpcReference(group = "defaultGroup",version = "1.0")
    private HelloService helloService;

    @GetMapping("/hello")
    @ResponseBody
    public String helloSay(){
        System.out.println(helloService.hello(new Hello("你好", "springboot欢迎你")));
        return "";
    }
}

package rpc.framework.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import rpc.framework.common.extension.ExtensionLoader;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.annotation.RpcReference;
import rpc.framework.annotation.RpcService;
import rpc.framework.config.RpcServiceConfig;
import rpc.framework.provider.ServiceProvider;
import rpc.framework.provider.impl.ZkServiceProviderImpl;
import rpc.framework.proxy.RpcClientProxy;
import rpc.framework.remoting.transport.RpcRequestTransport;

import java.lang.reflect.Field;


@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    //Spring bean在实例化之前会调用postProcessBeforeInitialization
    //在实例化之后会调用postProcessAfterInitialization
    //可以在bean在实例化之前会调用postProcessBeforeInitialization 来检查哪个类上有RpcService，然后注册到注册中心
    //在postProcessAfterInitialization 来检查哪个属性有RpcReference

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .service(bean)
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .build();

            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for(Field field : declaredFields){
            if(field.isAnnotationPresent(RpcReference.class)){
                RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .build();
                //获取代理类来执行
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object proxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}

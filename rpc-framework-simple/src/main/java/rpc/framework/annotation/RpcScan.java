package rpc.framework.annotation;


import org.springframework.context.annotation.Import;
import rpc.framework.spring.CustomScannerRegistrar;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {

    String[] basePackage();
}

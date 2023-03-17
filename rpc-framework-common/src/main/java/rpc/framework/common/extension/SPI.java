package rpc.framework.common.extension;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)         //注解保存多久    RUNTIME 运行时 CLASS 保留到class文件中，但jvm加载class文件时被丢弃
@Target(ElementType.TYPE)
public @interface SPI {
}

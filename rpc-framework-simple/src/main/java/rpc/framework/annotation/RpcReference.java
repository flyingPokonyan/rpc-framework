package rpc.framework.annotation;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    String version() default "";

    String group() default "";
}

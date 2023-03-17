package rpc.framework.serialize;


import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.extension.SPI;

/**
 * 序列化接口
 * 所有序列化操作都要实现此接口
 */

@SPI
public interface Serializer {


    /**
     * 序列化，将javaObj对象序列化为字节数组
     * @param obj
     * @return
     */
    byte[] serialize(Object obj);


    /**
     * 反序列化，将字节数组反序列化为目标类的对象
     * @param bytes
     * @param clazz 目标类
     * @param <T>   目标类型
     * @return
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}

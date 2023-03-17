package rpc.framework.serialize.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import rpc.framework.remoting.dto.RpcRequest;
import rpc.framework.remoting.dto.RpcResponse;
import rpc.framework.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 通过kryo实现序列化
 */
@Slf4j
public class KryoSerializer implements Serializer {


    //犹豫kryo是非线程安全的，因此使用ThreadLocal来保证线程安全
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Output output = new Output(bos)){
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output,obj);
            kryoThreadLocal.remove();
            return output.toBytes();

        }catch (Exception e){
            log.error("序列化异常");
            throw new RuntimeException("序列化异常");
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            Input input = new Input(bis)){
            Kryo kryo = kryoThreadLocal.get();
            T t = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return t;
        }catch (Exception e){
            log.error("序列化异常");
            throw new RuntimeException("序列化异常");
        }
    }
}

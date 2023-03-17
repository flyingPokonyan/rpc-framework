package rpc.framework.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import rpc.framework.common.factory.SingletonFactory;
import rpc.framework.remoting.dto.RpcRequest;
import rpc.framework.remoting.handler.RpcRequestHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{

    private final RpcRequestHandler rpcRequestHandler;
    private final Socket socket;
    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        this.socket = socket;
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())){
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            oos.writeObject(result);
            oos.flush();
        }catch (Exception e){
            log.error("occur exception:", e);
        }
    }
}

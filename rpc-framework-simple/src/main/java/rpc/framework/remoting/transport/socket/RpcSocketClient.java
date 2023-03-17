package rpc.framework.remoting.transport.socket;


import rpc.framework.common.extension.ExtensionLoader;
import rpc.framework.register.ServiceDiscovery;
import rpc.framework.remoting.dto.RpcRequest;
import rpc.framework.remoting.transport.RpcRequestTransport;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RpcSocketClient implements RpcRequestTransport {

    private final ServiceDiscovery serviceDiscovery;

    public RpcSocketClient() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);

        try(Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object o = objectInputStream.readObject();
            return o;
        }catch (Exception e){
            throw new RuntimeException("rpc服务调用失败");
        }
    }
}
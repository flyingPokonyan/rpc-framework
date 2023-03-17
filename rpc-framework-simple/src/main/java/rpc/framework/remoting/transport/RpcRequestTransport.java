package rpc.framework.remoting.transport;


import rpc.framework.common.extension.SPI;
import rpc.framework.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {

    /**
     * 发送rpc请求或获取rpc请求的结果
     * @param rpcRequest
     * @return
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}

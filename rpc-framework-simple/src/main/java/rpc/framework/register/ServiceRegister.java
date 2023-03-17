package rpc.framework.register;

import rpc.framework.common.extension.SPI;

import java.net.InetSocketAddress;


@SPI
public interface ServiceRegister {

    /**
     * 用于服务端注册服务
     * @param rpcServiceName        服务名称
     * @param inetSocketAddress     service 地址和端口号
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}

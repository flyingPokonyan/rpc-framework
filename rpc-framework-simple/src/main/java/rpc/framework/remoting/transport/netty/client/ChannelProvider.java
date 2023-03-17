package rpc.framework.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class ChannelProvider {

    private final Map<String, Channel> map;

    public ChannelProvider() {
        map = new ConcurrentHashMap<>();
    }

    //获取
    public Channel get(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        if(map.containsKey(key)){
            Channel channel = map.get(key);
            if(channel != null && channel.isOpen()){
                return channel;
            }
        }
        return null;
    }



    //添加
    public void set(InetSocketAddress inetSocketAddress,Channel channel){
        map.put(inetSocketAddress.toString(),channel);
    }


    //移除
    public void remove(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        map.remove(key);
        log.info("Channel map size :[{}]", map.size());
    }

}

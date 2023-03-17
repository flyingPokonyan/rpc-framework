package rpc.framework.remoting.transport.netty.client;

import rpc.framework.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId,CompletableFuture future){
        UNPROCESSED_RESPONSE_FUTURES.put(requestId,future);
    }

    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if(future != null){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException();
        }
    }
}

package rpc.framework.compress;


import rpc.framework.common.extension.SPI;


/**
 * 压缩、解压缩
 */
@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}

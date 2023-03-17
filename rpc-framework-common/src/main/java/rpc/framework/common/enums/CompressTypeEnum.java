package rpc.framework.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 0x01,"gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for(CompressTypeEnum e : CompressTypeEnum.values()){
            if(e.getCode() == code){
                return e.getName();
            }
        }
        return null;
    }

}

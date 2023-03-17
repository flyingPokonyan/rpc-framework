package rpc.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    KRYO((byte) 0x01,"kryo");


    private final byte code;
    private final String name;

    public static String getName(byte code){
        for(SerializationTypeEnum e : SerializationTypeEnum.values()){
            if(e.getCode() == code){
                return e.getName();
            }
        }
        return null;
    }

}

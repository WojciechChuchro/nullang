
package com.nullang.nullangobject;

public class BooleanType implements NullangObject{
    private final boolean value;
    

    public BooleanType(boolean value) {
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN_OBJ;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    
}


package com.nullang.nullangobject;

public class IntegerType implements NullangObject {
    private final int value;

    public int getValue() {
        return value;
    }

    public IntegerType(int value) {
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.INTEGER_OBJ;
    }

    @Override
    public String toString() {
        return "IntegerType [value=" + value + "]";
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    
}

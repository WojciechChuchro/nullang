
package com.nullang.nullangobject;

public record IntegerObject(int value) implements NullangObject {
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

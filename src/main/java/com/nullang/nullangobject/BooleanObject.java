
package com.nullang.nullangobject;

public record BooleanObject(boolean value) implements NullangObject{
    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN_OBJ;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }
}

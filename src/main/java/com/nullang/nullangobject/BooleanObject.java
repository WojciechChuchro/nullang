
package com.nullang.nullangobject;

public record BooleanObject(boolean value) implements NullangObject{
    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }
}

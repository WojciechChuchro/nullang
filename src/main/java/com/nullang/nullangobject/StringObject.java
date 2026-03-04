
package com.nullang.nullangobject;

public record StringObject(String value) implements NullangObject{
    @Override
    public ObjectType type() {
        return ObjectType.STRING;
    }

    @Override
    public String inspect() {
        return value;
    }
}

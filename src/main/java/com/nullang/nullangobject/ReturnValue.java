package com.nullang.nullangobject;

public record ReturnValue(
        NullangObject value) implements NullangObject {

    @Override
    public ObjectType type() {
        return ObjectType.RETURN_VALUE;
    }

    @Override
    public String inspect() {
        return this.value.inspect();
    }
}

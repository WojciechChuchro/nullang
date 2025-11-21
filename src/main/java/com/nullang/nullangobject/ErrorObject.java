package com.nullang.nullangobject;

public record ErrorObject(
        String message) implements NullangObject {

    @Override
    public ObjectType type() {
        return ObjectType.ERROR;
    }

    @Override
    public String inspect() {
        return "ERROR: " + message;
    }
}

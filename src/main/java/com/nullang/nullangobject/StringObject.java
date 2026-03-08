
package com.nullang.nullangobject;

public record StringObject(String value) implements NullangObject, Hashable {
    @Override
    public ObjectType type() {
        return ObjectType.STRING;
    }

    @Override
    public String inspect() {
        return value;
    }

    @Override
    public HashKey hashKey() {
        return new HashKey(this.type(), value.hashCode());
    }
}

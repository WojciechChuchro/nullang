
package com.nullang.nullangobject;

public record BooleanObject(boolean value) implements NullangObject, Hashable {
    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    @Override
    public HashKey hashKey() {
        return new HashKey(this.type(), this.value ? 1 : 0);
    }
}

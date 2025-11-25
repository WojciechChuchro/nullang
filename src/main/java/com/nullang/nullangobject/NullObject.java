
package com.nullang.nullangobject;

public class NullObject implements NullangObject{
    @Override
    public ObjectType type() {
        return ObjectType.NULL;
    }

    @Override
    public String inspect() {
        return "null";
    }
}

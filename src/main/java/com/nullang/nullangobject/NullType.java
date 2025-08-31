
package com.nullang.nullangobject;

public class NullType implements NullangObject{

    @Override
    public ObjectType type() {
        return ObjectType.NULL_OBJ;
    }

    @Override
    public String inspect() {
        return "null";
    }
}

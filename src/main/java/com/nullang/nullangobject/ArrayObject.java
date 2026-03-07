
package com.nullang.nullangobject;

import java.util.List;

public record ArrayObject(
        List<NullangObject> elements) implements NullangObject{
    @Override
    public ObjectType type() {
        return ObjectType.ARRAY;
    }

    @Override
    public String inspect() {
        return elements.toString();
    }
}


package com.nullang;

import com.nullang.nullangobject.NullangObject;
import com.nullang.nullangobject.ObjectType;

public record BuiltinObject() implements NullangObject {
    @Override
    public ObjectType type() {
        return ObjectType.BUILTIN;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}

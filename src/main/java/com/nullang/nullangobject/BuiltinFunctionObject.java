package com.nullang.nullangobject;

import java.util.List;
import java.util.function.Function;

public record BuiltinFunctionObject(
        Function<List<NullangObject>, NullangObject> function) implements NullangObject{

    public NullangObject call(List<NullangObject> args) {
        return function.apply(args);
    }

    @Override
    public ObjectType type() {
        return ObjectType.BUILTIN;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}
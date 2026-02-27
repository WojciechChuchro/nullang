package com.nullang.eval;

import com.nullang.nullangobject.NullangObject;

import java.util.HashMap;
import java.util.Map;

public class Env {
    private final Map<String, NullangObject> ENV = new HashMap<>();
    private final Env outer;

    public Env(Env outer) {
        this.outer = outer;
    }

    public void define(String name, NullangObject value) {
        ENV.put(name, value);
    }

    public NullangObject get(String name) {
        return ENV.get(name);
    }

    public boolean contains(String name) {
        return ENV.containsKey(name);
    }

}

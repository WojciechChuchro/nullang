package com.nullang.eval;

import com.nullang.nullangobject.NullangObject;

import java.util.HashMap;
import java.util.Map;

public class Env {
    private final Map<String, NullangObject> ENV = new HashMap<>();
    private Env outer = null;

    public Env() {}
    public Env(Env outer) {
        this.outer = outer;
    }

    public void define(String name, NullangObject value) {
        ENV.put(name, value);
    }

    public NullangObject get(String name) {
        if (!ENV.containsKey(name)) {
            if (outer != null) {
                return outer.get(name);
            }
        }
        return ENV.get(name);
    }

    public boolean contains(String name) {
        if (ENV.containsKey(name)) {
            return true;
        }
        if (outer != null) {
            return outer.contains(name);
        }
        return false;
    }

    public void setOuter(Env outer) {
        this.outer = outer;
    }
}

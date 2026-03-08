
package com.nullang.nullangobject;

import java.util.Map;

public record HashObject(
        Map<HashKey, HashPair> pairs) implements NullangObject{
    @Override
    public ObjectType type() {
        return ObjectType.HASH;
    }

    @Override
    public String inspect() {
        var buffer = new StringBuilder();
        buffer.append("{");

        var iterator = pairs.values().iterator();
        while (iterator.hasNext()) {
            var pair = iterator.next();
            buffer.append(pair.key().inspect());
            buffer.append(": ");
            buffer.append(pair.value().inspect());
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append("}");
        return buffer.toString();
    }
}

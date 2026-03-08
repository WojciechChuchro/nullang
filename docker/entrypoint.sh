#!/bin/sh
set -e

if [ "$1" = "run" ] && [ -n "$2" ]; then
    shift
    exec java -cp "/app/lib/*" com.nullang.RunFile "$@"
else
    exec /app/bin/nullang "$@"
fi

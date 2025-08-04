package com.nullang.parser;

public class Precedences {
    public static final int LOWEST = 1;
    public static final int EQUALS = 2;       // ==
    public static final int LESSGREATER = 3;  // > or <
    public static final int SUM = 4;          // +
    public static final int PRODUCT = 5;      // *
    public static final int PREFIX = 6;       // -X or !X
    public static final int CALL = 7;         // myFunction(X)
}

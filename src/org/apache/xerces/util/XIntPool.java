package org.apache.xerces.util;

public final class XIntPool {
    private static final short POOL_SIZE = 10;
    private static final XInt[] fXIntPool = new XInt[POOL_SIZE];

    static {
        for (int i = 0; i < POOL_SIZE; i++)
            fXIntPool[i] = new XInt(i);
    }

    public final XInt getXInt(int value) {
        if (value >= 0 && value < fXIntPool.length)
            return fXIntPool[value];
        else
            return new XInt(value);
    }
}

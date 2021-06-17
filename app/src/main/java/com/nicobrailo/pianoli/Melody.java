package com.nicobrailo.pianoli;

interface Melody {
    String nextNote();
    boolean hasNextNote();
    void reset();
}

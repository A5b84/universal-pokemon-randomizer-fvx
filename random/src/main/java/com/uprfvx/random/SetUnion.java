package com.uprfvx.random;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class SetUnion<E> extends AbstractSet<E> {

    private final Set<E> first;
    private final Set<E> second;

    public SetUnion(Set<E> first, Set<E> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean contains(Object o) {
        return first.contains(o) || second.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }
}

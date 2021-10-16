package com.jaoow.blockstop.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public final class Pair<L, R> {

    /**
     * Left object
     */
    public final L left;

    /**
     * Right object
     */
    public final R right;

    public L getKey() {
        return getLeft();
    }

    public R getValue() {
        return getRight();
    }
}

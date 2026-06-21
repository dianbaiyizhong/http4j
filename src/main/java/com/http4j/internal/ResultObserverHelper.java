package com.http4j.internal;

import com.http4j.ResultObserver;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Internal helper that tracks the global→local observer parent chain.
 * Used by {@link ResultObserver#defaultObserver()}.
 */
public final class ResultObserverHelper {

    private static final Map<ResultObserver, ResultObserver> parentMap = new WeakHashMap<>();

    private ResultObserverHelper() {
    }

    /**
     * Record the parent (global) observer for a child (local) observer.
     */
    public static void setParent(ResultObserver child, ResultObserver parent) {
        if (parent != null) {
            parentMap.put(child, parent);
        }
    }

    /**
     * Retrieve the parent (global) observer for the given observer, or {@code null}.
     */
    public static ResultObserver getParent(ResultObserver child) {
        return parentMap.get(child);
    }
}

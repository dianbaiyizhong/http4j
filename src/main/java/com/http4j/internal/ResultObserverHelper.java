package com.http4j.internal;

import com.http4j.ResultObserver;
import java.util.Map;
import java.util.WeakHashMap;

public final class ResultObserverHelper {

    private static final Map<ResultObserver, ResultObserver> parentMap = new WeakHashMap<>();

    private ResultObserverHelper() {}

    public static void setParent(ResultObserver child, ResultObserver parent) {
        if (parent != null) parentMap.put(child, parent);
    }

    public static ResultObserver getParent(ResultObserver child) {
        return parentMap.get(child);
    }
}

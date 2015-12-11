package org.droidphy.core.utils;

import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.ParameterizedType;

public class Util {
    public static <T> Class<T> resolveGenericTypeClass(Class<?> baseClass) {
        return (Class<T>) TypeFactory.rawClass(
                ((ParameterizedType) baseClass.getGenericSuperclass())
                        .getActualTypeArguments()[0]);
    }
}

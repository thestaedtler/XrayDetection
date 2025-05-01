package org.cl.xrayDetection.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface TypeToken<T> {
    default Type type() {
        return ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }

    class Basic<T> implements TypeToken<T> {
        private final Class<T> clazz;

        public Basic(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Class<T> type() {
            return clazz;
        }
    }
}

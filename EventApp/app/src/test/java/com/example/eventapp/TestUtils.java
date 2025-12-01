package com.example.eventapp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TestUtils {

    @SuppressWarnings("unchecked")
    public static List<Event> getPrivateList(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (List<Event>) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokePrivate(Object target, String methodName) {
        try {
            Method m = target.getClass().getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

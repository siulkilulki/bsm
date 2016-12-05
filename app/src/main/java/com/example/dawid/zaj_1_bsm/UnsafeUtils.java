package com.example.dawid.zaj_1_bsm;

import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by dawid on 08.11.16.
 */
public class UnsafeUtils {

    private static Object getInstance(){

        try {
            Class clazz = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = clazz.getDeclaredField("THE_ONE");
            theUnsafe.setAccessible(true);
            return theUnsafe.get(null);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long addressOf(Object o) {
        final Object[] array = new Object[] {o};

        final Object unsafe = getInstance();
        try {
            final Method arrayBaseOffsetMethod = unsafe.getClass().getMethod("arrayBaseOffset", Class.class);
            int arrayBaseOffset = (int)arrayBaseOffsetMethod.invoke(unsafe, Object[].class);
            final Method getIntMethod = unsafe.getClass().getMethod("getInt", Object.class, long.class);
            return (int)getIntMethod.invoke(unsafe, array, arrayBaseOffset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void deleteSecretKey(Object o) {
        final Object unsafe = getInstance();
        try {
            SecretKeySpec secretKeySpec = (SecretKeySpec) o;
            Field field = secretKeySpec.getClass().getDeclaredField("key");
            final Method objectFieldOffsetMethod = unsafe.getClass().getMethod("objectFieldOffset", Field.class);
            final Method getObjectMethod = unsafe.getClass().getMethod("getObject", Object.class, long.class);

            long offset = (long)objectFieldOffsetMethod.invoke(unsafe, field);
            byte[] key = (byte [])getObjectMethod.invoke(unsafe, secretKeySpec, offset);

            final Method arrayBaseOffsetMethod = unsafe.getClass().getMethod("arrayBaseOffset", Class.class);
            final Method arrayIndexScaleMethod = unsafe.getClass().getMethod("arrayIndexScale", Class.class);

            final Method putInt = unsafe.getClass().getMethod("putInt", Object.class, long.class, int.class);

            int base = (int) arrayBaseOffsetMethod.invoke(unsafe, Byte.class);
            int scale = (int) arrayIndexScaleMethod.invoke(unsafe, Byte.class);
            for (int i = 0; i < key.length; i++) {
                putInt.invoke(unsafe, key, (base + i * scale), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


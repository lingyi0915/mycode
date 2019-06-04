package com.hjh.java.reflect;

/**
 * @Author: hjh
 * @Create: 2019/4/18
 * @Description:
 */
public class GetClassType {

    public static String getPrimitiveClassType(Class clazz){
        return clazz.getName();
    }

    public static void main(String[] args) {

        System.out.println(getPrimitiveClassType(boolean.class));
        System.out.println(getPrimitiveClassType(byte.class));
        System.out.println(getPrimitiveClassType(char.class));
        System.out.println(getPrimitiveClassType(short.class));
        System.out.println(getPrimitiveClassType(int.class));
        System.out.println(getPrimitiveClassType(long.class));
        System.out.println(getPrimitiveClassType(float.class));
        System.out.println(getPrimitiveClassType(double.class));

        System.out.println(getPrimitiveClassType(Boolean.class));
        System.out.println(getPrimitiveClassType(Byte.class));
        System.out.println(getPrimitiveClassType(Character.class));
        System.out.println(getPrimitiveClassType(Short.class));
        System.out.println(getPrimitiveClassType(Integer.class));
        System.out.println(getPrimitiveClassType(Long.class));
        System.out.println(getPrimitiveClassType(Float.class));
        System.out.println(getPrimitiveClassType(Double.class));



    }
}

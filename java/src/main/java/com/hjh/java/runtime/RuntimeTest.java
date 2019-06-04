package com.hjh.java.runtime;

public class RuntimeTest {
    public static void main(String[] args) {
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println(processors);
    }
}

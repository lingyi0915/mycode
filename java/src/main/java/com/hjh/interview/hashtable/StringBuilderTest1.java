package com.hjh.interview.hashtable;

public class StringBuilderTest1 {
    public static void main(String[] args) {
        int n = 10000000;
        long sbst = System.currentTimeMillis();
        //线程不安全
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < n ; i++) {
            sb.append("1");
        }
        long sbet = System.currentTimeMillis();
        //线程安全
        StringBuffer sbuffer = new StringBuffer();
        for(int i = 0 ; i < n ; i++){
            sbuffer.append("1");
        }
        long sbet2 = System.currentTimeMillis();
        System.out.println((sbet-sbst)+","+(sbet2-sbet));

    }
}
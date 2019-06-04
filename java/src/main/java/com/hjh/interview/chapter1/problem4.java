package com.hjh.interview.chapter1;


import java.util.Arrays;

public class problem4 {
    public static void solution1(char[] str){
        int spaceNum=0;
        int textLength=0;
        while(str[textLength]!=0){
            if(str[textLength]==' '){
                spaceNum++;
            }
            textLength++;
        }
        print(str,textLength);

        if(spaceNum == 0){
            return ;
        }
        int moveNum = 2*spaceNum;
        int newlen = textLength+moveNum;
        while(textLength>=0){
            int tmpLen = textLength+moveNum;
            if(str[textLength]==' '){
                str[tmpLen-2]='%';
                str[tmpLen-1]='2';
                str[tmpLen]='0';
                moveNum-=2;
            }else{
                str[tmpLen]=str[textLength];
            }
            textLength--;
        }
        print(str,newlen);
    }

    public static void print(char[] str,int len){
        for(int i= 0 ; i < len ; i++){
            System.out.print(str[i]);
        }
        System.out.println();
    }


    public static void main(String[] args) {
        String s = "Mr John Smith   ";
        char[] str = new char[10000];
        solution1(Arrays.copyOf(s.toCharArray(),10000));
    }

}

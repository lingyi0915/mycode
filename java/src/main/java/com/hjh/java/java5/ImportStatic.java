package com.hjh.java.java5;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-04-20
 * @Description: 精确的导入一个静态成员的方法，是在源文件的开头部分（任何类或接口的定义之前），加上类似这样的声明：
 * import static 包名.类或接口名.静态成员名;
 * 注意尽管这个机制的名目是叫做“Static Import”，
 * 但是在这里的次序却是正好相反的“import static”。
 * 一经导入之后，在整个源文件的范围内，就可以直接用这个成员的名字来访问它了。
 */
public class ImportStatic {
  public static void main(String[] args) {
    System.out.println(sin(PI/2));//输出“1.0”
  }
}

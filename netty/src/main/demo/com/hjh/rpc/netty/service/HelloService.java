package com.hjh.rpc.netty.service;

/**
 * @Author: hjh
 * @Create: 2019/4/9
 * @Description:
 */
public class HelloService implements Service{
    @Override
    public String start(String name) {
        return sayHello(name);
    }

    public String sayHello(String name){
        return name!=null?"hello,"+name:"hello world!";
    }
}

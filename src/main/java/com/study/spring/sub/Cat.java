package com.study.spring.sub;

import com.study.spring.AutoWired;
import com.study.spring.Component;
import com.study.spring.PostConstruct;

@Component
public class Cat {
    public String name;

    public int age;

    @AutoWired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("Cat创建了 cat里面有一个属性" + dog);
    }
}

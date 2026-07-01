package com.study.spring.sub;

import com.study.spring.Autowired;
import com.study.spring.Component;
import com.study.spring.PostConstruct;

@Component
public class Dog {
    public String name;

    public int age;

    @Autowired
    private Cat cat;

    @PostConstruct
    public void init() {
        System.out.println("Dog创建了 dog里面有一个属性" + cat);
    }
  }

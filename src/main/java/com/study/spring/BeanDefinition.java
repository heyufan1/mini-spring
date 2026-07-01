package com.study.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public class BeanDefinition {
    private String name;
    private Constructor<?> constructor;
    private Method PostConstruct;

    public BeanDefinition(Class<?> type) {
        Component components = type.getDeclaredAnnotation(Component.class);
        this.name = components.name().isEmpty() ? type.getSimpleName() : components.name();
        try {
            this.constructor = type.getConstructor();
            this.PostConstruct =
                    Arrays.stream(type.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                            .findFirst().orElse(null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public String getName() {
        return name;
    }

    public Constructor<?> getConstructor() {
        return this.constructor;
    }

    public Method getPostConstructMethod() {
        return PostConstruct;
    }
}

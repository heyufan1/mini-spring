package com.study.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class BeanDefinition {
    private final String name;
    private final Constructor<?> constructor;
    private final Method postconstructMethods;
    private final List<Field> autowiredFields;
    private final Class<?> beanType;



    public BeanDefinition(Class<?> type) {
        Component components = type.getDeclaredAnnotation(Component.class);
        this.name = components.name().isEmpty() ? type.getSimpleName() : components.name();
        this.autowiredFields =
                Arrays.stream(type.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(Autowired.class))
                        .toList();
        this.beanType = type;
        try {
            this.constructor = type.getConstructor();
            this.postconstructMethods =
                    Arrays.stream(type.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                            .findFirst().orElse(null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Field> getAutowiredFields() {
        return autowiredFields;
    }

    public String getName() {
        return name;
    }

    public Constructor<?> getConstructor() {
        return this.constructor;
    }

    public Method getPostConstructMethod() {
        return postconstructMethods;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

}

package com.study.spring;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContext {

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Object> loadingIoc = new HashMap<>();

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    public void initContext(String packageName) throws Exception {
        scanPackage(packageName).stream().filter(this::scanCreate).forEach(this::wrapper);
        beanDefinitionMap.values().forEach(this::createBean);
    }

    protected boolean scanCreate(Class<?> type) {
        return type.isAnnotationPresent(Component.class);
    }

    protected Object createBean(BeanDefinition beanDefinition) {
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return ioc.get(name);
        }
        if (loadingIoc.containsKey(name)) {
            return loadingIoc.get(name);
        }
        return doCreateBean(beanDefinition);

    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            autowiredBean(bean, beanDefinition);
            Method postConstructMethod = beanDefinition.getPostConstructMethod();
            if (postConstructMethod != null) {
                postConstructMethod.invoke(bean);
            }
            ioc.put(beanDefinition.getName(), loadingIoc.remove(beanDefinition.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    private void autowiredBean(Object bean, BeanDefinition beanDefinition) throws IllegalAccessException {
        for (Field autowiredField : beanDefinition.getAutowiredFields()) {
            autowiredField.setAccessible(true);
            Object autowireBean = null;
            autowiredField.set(bean, getBean(autowiredField.getType()));
        }
    }

    protected BeanDefinition wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("beanName repeat");
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    public List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        URL resource = this.getClass().getClassLoader().getResource(packageName.replace(".", File.separator));
        Path path = Path.of(resource.toURI());
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".class")) {
                    String replaceStr = absolutePath.toString().replace(File.separator, ".");
                    int packageIndex = replaceStr.indexOf(packageName);
                    String allClassName = replaceStr.substring(packageIndex, replaceStr.length() - ".class".length());
                    try {
                        classList.add(Class.forName(allClassName));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classList;
    }

    public Object getBean(String name) {
        if (name == null) {
            return null;
        }
        Object bean = this.ioc.get(name);
        if (bean != null) {
            return bean;
        }
        if (beanDefinitionMap.containsKey(name)) {
            return createBean(beanDefinitionMap.get(name));
        }
        return null;
    }

    public <T> T getBean(Class<T> beanType) {
        String beanName =
                this.beanDefinitionMap.values().stream()
                        .filter(bd -> beanType.isAssignableFrom(bd.getBeanType()))
                        .map(BeanDefinition::getName)
                        .findFirst()
                        .orElse(null);
        return (T) getBean(beanName);
    }


    public <T> List<T> geBean(Class<T> beanType) {
        return this.beanDefinitionMap.values().stream()
                .filter(bd -> beanType.isAssignableFrom(bd.getBeanType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map((bean) -> (T) bean)
                .toList();
    }


}

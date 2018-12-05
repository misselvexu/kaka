package com.kaka;

import com.kaka.notice.register.ModelRegister;
import com.kaka.notice.register.IRegister;
import com.kaka.notice.register.CommandRegister;
import com.kaka.notice.register.MediatorRegister;
import com.kaka.util.ClassScaner;
import java.util.Collections;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import java.util.Map;

/**
 * 启动器，其中包含类扫描及事件通知模型的注册
 *
 * @author zhoukai
 */
public abstract class Startup {

    final Map<String, IRegister> registerList = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * 构造方法
     *
     * @param registers 事件通知模型注册器
     */
    public Startup(IRegister... registers) {
        addRegister(new CommandRegister());
        addRegister(new MediatorRegister());
        addRegister(new ModelRegister());
        for (IRegister register : registers) {
            addRegister(register);
        }
    }

    /**
     * 添加事件通知模型注册器
     *
     * @param register 事件通知模型注册器
     */
    final protected void addRegister(IRegister register) {
        String name = register.name();
        if (name == null) {
            name = register.getClass().getTypeName();
        }
        registerList.put(name, register);
    }

    /**
     * 初始化
     *
     * @param loader 从此类加载器中扫描加载类
     * @param packages .分割的包名
     * @return 扫描到的类集合
     */
    final protected Set<Class<?>> scan(ClassLoader loader, String... packages) {
        Set<Integer> delIdxs = new HashSet<>();
        //过滤子包和相同的包
        for (int i = 0; i < packages.length; i++) {
            String selectPackage = packages[i];
            for (int j = i + 1; j < packages.length; j++) {
                String currPackage = packages[j];
                if (selectPackage.length() < currPackage.length()) {
                    if (currPackage.contains(selectPackage)) {
                        delIdxs.add(j);
                    }
                } else if (selectPackage.length() > currPackage.length()) {
                    if (selectPackage.contains(currPackage)) {
                        delIdxs.add(i);
                    }
                } else if (selectPackage.equals(currPackage)) {
                    delIdxs.add(j);
                }
            }
        }
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        Set<Class<?>> classes = null;
        for (int i = 0; i < packages.length; i++) {
            if (!delIdxs.contains(i)) {
                classes = ClassScaner.getClasses(loader, packages[i]);
                classes.forEach((Class<?> cls) -> {
                    registerList.forEach((String name, IRegister register) -> {
                        register.regist(cls);
                    });
                });
            }
        }
        return classes;
    }

    /**
     * 初始化
     *
     * @param packages .分割的包名
     * @return 扫描到的类集合
     */
    final protected Set<Class<?>> scan(String... packages) {
        return scan(null, packages);
    }

}
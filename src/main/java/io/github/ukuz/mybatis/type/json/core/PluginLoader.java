/*
 * Copyright 2019 ukuz90
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ukuz.mybatis.type.json.core;

import io.github.ukuz.mybatis.type.json.compiler.Compiler;
import io.github.ukuz.mybatis.type.json.extension.ClassCodeGenerator;
import io.github.ukuz.mybatis.type.json.utils.ClassHelper;
import io.github.ukuz.mybatis.type.json.utils.Holder;
import io.github.ukuz.mybatis.type.json.utils.StringUtils;
import org.springframework.beans.factory.BeanFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ukuz90
 * @since 2019-05-16
 */
public class PluginLoader<T> {

    private final static ConcurrentHashMap<Class<?>, PluginLoader> LOADERS = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Class<?>, Holder<Object>> INSTANCES = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<T>>> cachedClassHolder = new Holder<>();
    private final Holder<Map<String, Class<T>>> cachedGenerateClassHolder = new Holder<>();

    private final Class<T> type;
    private final String defaultKey;
    private final static String PLUGIN_SERVICES = "META-INF/services";
    private final static String PLUGIN_UKUZ = "META-INF/ukuz";

    private PluginLoader(Class<T> type) {
        this.type = type;
        this.defaultKey = type.getAnnotation(Spi.class).value();
    }

    public static PluginLoader getLoader(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Plugin class must not be null");
        }
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("Plugin class " + clazz + " is not an interface!");
        }
        if (!clazz.isAnnotationPresent(Spi.class)) {
            throw new IllegalArgumentException("Plugin class " + clazz + " must annotated with @Spi");
        }
        PluginLoader loader = LOADERS.get(clazz);
        if (loader == null) {
            LOADERS.putIfAbsent(clazz, new PluginLoader(clazz));
            loader = LOADERS.get(clazz);
        }
        return loader;

    }

    public T getDefaultPlugin() {
        if (StringUtils.isEmpty(this.defaultKey)) {
            throw new IllegalArgumentException("Plugin class " + type + " have not default implementation");
        }
        return getPlugin(defaultKey);
    }

    public T getPlugin(String key) {
        return getPlugin(key, null);
    }

    public T getGeneratePlugin(String className) {
        getPluginClass();
        Map<String, Class<T>> pluginClass = getGeneratePluginClass();
        Class<T> clazz = pluginClass.get(className);
        if (clazz == null) {
            clazz = createGeneratePlugin(className);
            pluginClass.put(className, clazz);
        }
        Holder holder = INSTANCES.get(clazz);
        if (holder == null) {
            INSTANCES.putIfAbsent(clazz, new Holder<>());
            holder = INSTANCES.get(clazz);
        }
        return getInstance(holder, clazz, null);
    }

    public T getPlugin(String key, BeanFactory beanFactory) {
        Map<String, Class<T>> pluginClass = getPluginClass();
        Class<T> clazz = pluginClass.get(key);
        if (clazz == null) {
            throw new IllegalArgumentException("Can not found " + key + " mapping class");
        }

        Holder holder = INSTANCES.get(clazz);
        if (holder == null) {
            INSTANCES.putIfAbsent(clazz, new Holder<>());
            holder = INSTANCES.get(clazz);
        }
        return getInstance(holder, clazz, beanFactory);
    }

    private T getInstance(Holder holder, Class<T> clazz, BeanFactory beanFactory) {
        Object obj = holder.getVal();
        if (obj == null) {
            synchronized (holder) {
                obj = holder.getVal();
                if (obj == null) {
                    try {
                        obj = newInstance(clazz, beanFactory);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                    holder.setVal(obj);
                }
            }
        }
        return (T) obj;
    }

    private Object newInstance(Class<?> clazz, BeanFactory beanFactory) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor[] constructors = clazz.getConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getParameters().length > 0) {
                Class[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                if (beanFactory != null) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (parameterTypes[i].isAssignableFrom(beanFactory.getClass())) {
                            parameters[i] = beanFactory;
                        } else {
                            parameters[i] = beanFactory.getBean(parameterTypes[i]);
                        }
                    }
                }
                return constructor.newInstance(parameters);
            } else {
               return constructor.newInstance(null);
            }
        }
        return null;
    }

    private Map<String, Class<T>> getPluginClass() {
        Map<String, Class<T>> classMap = this.cachedClassHolder.getVal();
        if (classMap == null) {
            synchronized (this.cachedClassHolder) {
                classMap = this.cachedClassHolder.getVal();
                if (classMap == null) {
                    classMap = loadPluginClass();
                    this.cachedClassHolder.setVal(classMap);
                }
            }
        }
        return classMap;
    }

    private Map<String, Class<T>> getGeneratePluginClass() {
        Map<String, Class<T>> classMap = this.cachedGenerateClassHolder.getVal();
        if (classMap == null) {
            synchronized (this.cachedGenerateClassHolder) {
                classMap = this.cachedGenerateClassHolder.getVal();
                if (classMap == null) {
                    classMap = new HashMap<>();
                    this.cachedGenerateClassHolder.setVal(classMap);
                }
            }
        }
        return classMap;
    }

    private Map<String, Class<T>> loadPluginClass() {
        Map<String, Class<T>> pluginClasses = new HashMap<>();
        loadDirectory(pluginClasses, PLUGIN_UKUZ);
        loadDirectory(pluginClasses, PLUGIN_SERVICES);
        return pluginClasses;
    }

    private void loadDirectory(Map<String, Class<T>> pluginClasses, String dir) {
        String fileName = dir + "/" + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls == null) {
                return;
            }
            Collections.list(urls).forEach(url -> {
                if (url != null) {
                    loadResource(pluginClasses, url);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadResource(Map<String, Class<T>> pluginClasses, URL url) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line ;
            while((line = br.readLine()) != null) {
                int index = line.indexOf("#");
                if (index != -1) {
                    line = line.substring(0, index);
                }
                line = line.trim();
                String key = null;
                index = line.indexOf("=");
                if (index != -1) {
                    key = line.substring(0, index).trim();
                    line = line.substring(index+1).trim();
                }
                if (line.length() > 0) {
                    loadClass(pluginClasses, key, line);
                }
            }
        } catch (Exception e) {
        }
    }

    private void loadClass(Map<String, Class<T>> pluginClasses, String key, String className) {
        try {
            Class clazz = Class.forName(className);
            if (key != null && !key.equals("")) {
                key = getPrefixClassName(clazz);
            }
            pluginClasses.putIfAbsent(key, clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getPrefixClassName(Class clazz) {
        String prefix;
        int index = clazz.getSimpleName().indexOf(type.getSimpleName());
        if (index != -1) {
            prefix = clazz.getSimpleName().substring(0, index).toLowerCase();
        } else {
            prefix = clazz.getSimpleName().toLowerCase();
        }
        return prefix;
    }

    private ClassLoader findClassLoader() {
        return ClassHelper.getClassLoader(PluginLoader.class);
    }

    private Class<T> createGeneratePlugin(String className) {
        String code = new ClassCodeGenerator(type, className).generate();
        ClassLoader classLoader = findClassLoader();
        Compiler compiler = (Compiler) PluginLoader.getLoader(Compiler.class).getDefaultPlugin();
        return (Class<T>) compiler.compile(code, classLoader);
    }

}

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

import io.github.ukuz.mybatis.type.json.annotation.JsonString;
import io.github.ukuz.mybatis.type.json.type.ListTypeHandler;
import io.github.ukuz.mybatis.type.json.utils.AnnotationUtils;
import io.github.ukuz.mybatis.type.json.utils.ClassHelper;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.List;
import java.util.stream.Stream;


/**
 * @author ukuz90
 * @since 2019-06-27
 */
public class AdditionalTypeHandlerRegistry {

    private volatile Configuration configuration;
    private final static Object PLACE = new Object();
    private ConcurrentReferenceHashMap<Class, Object> delayRegistryClass = new ConcurrentReferenceHashMap(256);

    private static final Logger LOGGER = LoggerFactory.getLogger(AdditionalTypeHandlerRegistry.class);

    public AdditionalTypeHandlerRegistry() {
    }

    public void startScan(String[] scanPackages) throws URISyntaxException, ClassNotFoundException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("AdditionalTypeHandlerRegistry start startScan {}", scanPackages);
        }
        EntityClassPathScanner entityClassPathScanner = new EntityClassPathScanner();
        Set<Class> classSet = entityClassPathScanner.doScan(scanPackages);
        classSet.forEach(clazz -> {
            Field[] fields = clazz.getDeclaredFields();
            Stream.of(fields)
                    .filter(field -> field.isAnnotationPresent(JsonString.class))
                    .forEach(this::processJsonStringField);
        });
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("AdditionalTypeHandlerRegistry finish startScan {}", scanPackages);
        }
    }

    private void processJsonStringField(Field field) {
        if (AnnotationUtils.isAssignableFrom(List.class, field.getType())) {
            try {
                Class<?> domainClass = findDomainClass(field);
                ListTypeHandler listTypeHandler = (ListTypeHandler) PluginLoader.getLoader(ListTypeHandler.class)
                        .getGeneratePlugin(domainClass.getName());
                delayRegistryClass.putIfAbsent(listTypeHandler.getClass(), PLACE);
//                typeHandlerRegistry.register(listTypeHandler.getClass());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void setConfiguration(Configuration configuration) {
        if (configuration == null) {
            synchronized (this) {
                if (configuration == null) {
                    this.configuration = configuration;
                    delayRegistry();
                }
            }
        }
    }

    private void delayRegistry() {
        delayRegistryClass.keySet().forEach(configuration.getTypeHandlerRegistry()::register);
    }

    private Class<?> findDomainClass(Field field) throws ClassNotFoundException {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = ((ParameterizedType) type);
            Type[] actualTypes = pt.getActualTypeArguments();
            return getClass(actualTypes[0].getTypeName());
        }
        return null;
    }

    private Class<?> getClass(String className) throws ClassNotFoundException {
        return findClassLoader().loadClass(className);
    }

    private ClassLoader findClassLoader() {
        return ClassHelper.getClassLoader(AdditionalTypeHandlerRegistry.class);
    }
}

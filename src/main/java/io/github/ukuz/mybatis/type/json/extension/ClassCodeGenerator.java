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
package io.github.ukuz.mybatis.type.json.extension;

import io.github.ukuz.mybatis.type.json.core.PluginLoader;
import io.github.ukuz.mybatis.type.json.utils.AnnotationUtils;
import io.github.ukuz.mybatis.type.json.utils.ClassUtils;
import io.github.ukuz.mybatis.type.json.utils.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * @author ukuz90
 * @since 2019-06-27
 */
public class ClassCodeGenerator {

    private Class<?> type;
    private String className;

    public static final String PACKAGE_FORMAT = "package %s;\n\n";
    public static final String NOTE_FORMAT = "/**\n * @author ukuz90\n */\n";
    public static final String CLASS_FORMAT = "public class %s implements %s {\n\n%s\n}";
    public static final String CLASS_FORMAT_2 = "public class %s extends %s {\n\n%s\n}";
    public static final String METHOD_FORMAT = "%s %s %s(%s) {\n%s\n}\n\n";
    public static final String DYNAMIC_CLASS_SEPERATOR = "$";

    public ClassCodeGenerator(Class<?> type, String className) {
        this.type = type;
        this.className = className;
    }

    public String generate() {
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type + " must a interface");
        }
        StringBuilder sb = new StringBuilder();

        sb.append(generatePackage());
        sb.append(generateNote());
        sb.append(generateClassAnnotation());
        sb.append(generateClass());

        return sb.toString();
    }

    private String generateNote() {
        return NOTE_FORMAT;
    }

    private String generateClassAnnotation() {
        if (AnnotationUtils.isAssignableFrom(TypeHandler.class, type)) {
            StringBuilder classAnnotation = new StringBuilder();
            classAnnotation.append(String.format("@%s({%s.VARCHAR})", MappedJdbcTypes.class.getName(), JdbcType.class.getName()));
            classAnnotation.append("\n");
            classAnnotation.append(String.format("@%s({%s})", MappedTypes.class.getName(), "java.util."+ getPrefixClassName(TypeHandler.class, type) +".class"));
            classAnnotation.append("\n");
            return classAnnotation.toString();
        }
        return "";
    }

    private String generateClass() {
        StringBuilder classBody = new StringBuilder();

        String classFormat = null;
        if (AnnotationUtils.isAssignableFrom(TypeHandler.class, type)) {
            classFormat = CLASS_FORMAT_2;
        } else {
            Stream.of(type.getDeclaredMethods()).forEach(method ->
                    classBody.append(generateMethod(method))
            );
            classFormat = CLASS_FORMAT;
        }

        return String.format(classFormat, getClassName(), getInterfaceName(), classBody.toString());
    }

    private String generateMethod(Method method) {
        String modifiers;
        String returnTypes;
        String methodName;
        ArrayList<String> parameters = new ArrayList();
        String methodBody;

        modifiers = "public";
        //returnTypes
        Class<?> returnType = method.getReturnType();
        returnTypes = returnType.getName();
        //methodName
        methodName = method.getName();
        //parameters
        Parameter[] params = method.getParameters();
        for (Parameter param : params) {
            parameters.add(param.getType().getName() + " " + param.getName());
        }
        //methodBody
        methodBody = "\tSystem.out.println(" + method.getName() + ");";

        return String.format(METHOD_FORMAT, modifiers, returnTypes, methodName, StringUtils.join(parameters, ", "), methodBody);
    }

    private String generatePackage() {
        return String.format(PACKAGE_FORMAT, type.getPackage().getName());
    }

    private String getClassName() {
        String newClassName = ClassUtils.getSimpleClassName(type.getName()) + DYNAMIC_CLASS_SEPERATOR
                + ClassUtils.getSimpleClassName(className);
        return newClassName;
    }

    private String getInterfaceName() {
        if (AnnotationUtils.isAssignableFrom(TypeHandler.class, type)) {
            String interfaceType = ClassUtils.getSimpleClassName(PluginLoader.getLoader(type).getDefaultPlugin().getClass().getName());
            String genericType = className;
            return String.format("%s<%s>", interfaceType, genericType);
        } else {
            return ClassUtils.getSimpleClassName(type.getName());
        }
    }

    private String getPrefixClassName(Class<?> baseClass, Class<?> implClass) {
        String prefix;
        int index = implClass.getSimpleName().indexOf(baseClass.getSimpleName());
        if (index != -1) {
            prefix = implClass.getSimpleName().substring(0, index);
        } else {
            prefix = baseClass.getSimpleName();
        }
        return prefix;
    }

}

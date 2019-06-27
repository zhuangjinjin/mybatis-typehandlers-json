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

import io.github.ukuz.mybatis.type.json.annotation.AnnotationTypeFilter;
import io.github.ukuz.mybatis.type.json.utils.ClassHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ukuz90
 * @since 2019-06-27
 */
public class EntityClassPathScanner {

    private Set<AnnotationTypeFilter> filters = new HashSet<>();

    public void addFilter(AnnotationTypeFilter filter) {
        this.filters.add(filter);
    }

    public Set<Class> doScan(String[] scanPackages) throws ClassNotFoundException, URISyntaxException {
        Set<Class> result = new HashSet<>();
        for (String scanPackage : scanPackages) {
            String path = scanPackage.replaceAll("\\.", File.separator);
            File file = new File(findClassLoader().getResource(path).toURI());
            File[] childrenFiles = file.listFiles();
            for (File children : childrenFiles) {
                String className = scanPackage + "." + children.getName().split(".class")[0];
                Class clazz = findClassLoader().loadClass(className);
                if (!filters.isEmpty()) {
                    for (AnnotationTypeFilter filter : filters) {
                        if (filter.match(clazz)) {
                            result.add(clazz);
                        }
                    }
                } else {
                    result.add(clazz);
                }
            }
        }

        return result;
    }

    public ClassLoader findClassLoader() {
        return ClassHelper.getClassLoader(EntityClassPathScanner.class);
    }

}

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
package io.github.ukuz.mybatis.type.json.serializable;

import io.github.ukuz.mybatis.type.json.core.Spi;

import java.util.List;

/**
 * @author ukuz90
 * @since 2019-06-26
 */
@Spi
public interface JsonSerializer {

    /**
     * 把json字符串反序列成List对象
     * @param text json字符串
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> parseList(String text, Class<T> clazz);

    /**
     * 把json字符串反序列成普通的java对象
     * @param text json字符串
     * @param clazz
     * @return
     */
    <T> T parseObject(String text, Class<T> clazz);

    /**
     * 把java对象序列化成json字符串
     * @param obj java对象
     * @return json字符串
     */
    <T> String toJsonString(T obj);

}

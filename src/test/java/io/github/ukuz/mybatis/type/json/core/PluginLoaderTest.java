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

import io.github.ukuz.mybatis.type.json.type.DefaultListTypeHandler;
import io.github.ukuz.mybatis.type.json.type.ListTypeHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginLoaderTest {

    @Test
    void getPlugin() {

    }

    @Test
    void getDefaultPlugin() {
        ListTypeHandler listTypeHandler = (ListTypeHandler) PluginLoader.getLoader(ListTypeHandler.class).getDefaultPlugin();
        assertTrue(listTypeHandler.getClass() == DefaultListTypeHandler.class);
    }

    @Test
    void getGeneratePlugin() {
        ListTypeHandler listTypeHandler = (ListTypeHandler) PluginLoader.getLoader(ListTypeHandler.class).getGeneratePlugin(Staff.class.getName());
        assertEquals(listTypeHandler.getClass().getName(), "io.github.ukuz.mybatis.type.json.type.ListTypeHandler$Staff");
    }
}
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

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author ukuz90
 * @since 2019-06-26
 */
public class FastJsonSerializer implements JsonSerializer {
    @Override
    public <T> List<T> parseList(String text, Class<T> clazz) {
        return JSONObject.parseArray(text, clazz);
    }

    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        return JSONObject.parseObject(text, clazz);
    }

    @Override
    public <T> String toJsonString(T obj) {
        return JSONObject.toJSONString(obj);
    }
}

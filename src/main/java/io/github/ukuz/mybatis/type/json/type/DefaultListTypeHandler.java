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
package io.github.ukuz.mybatis.type.json.type;

import io.github.ukuz.mybatis.type.json.core.PluginLoader;
import io.github.ukuz.mybatis.type.json.serializable.JsonSerializer;
import io.github.ukuz.mybatis.type.json.utils.ClassHelper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author ukuz90
 * @since 2019-06-27
 */
public class DefaultListTypeHandler<T> extends BaseTypeHandler<List> implements ListTypeHandler<List> {

    private JsonSerializer jsonSerializer;

    {
        jsonSerializer = (JsonSerializer) PluginLoader.getLoader(JsonSerializer.class).getPlugin("fast");
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, jsonSerializer.toJsonString(parameter));
    }

    @Override
    public List getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return jsonSerializer.parseList(rs.getString(columnName), getActualTypeClass());
    }

    @Override
    public List getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return jsonSerializer.parseList(rs.getString(columnIndex), getActualTypeClass());
    }

    @Override
    public List getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return jsonSerializer.parseList(cs.getString(columnIndex), getActualTypeClass());
    }

    private Class getActualTypeClass() {
        try {
            return findClassLoader().loadClass(this.getRawType().getTypeName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Object.class;
    }

    private ClassLoader findClassLoader() {
        return ClassHelper.getClassLoader(DefaultListTypeHandler.class);
    }
}

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

import io.github.ukuz.mybatis.type.json.core.Spi;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author ukuz90
 * @since 2019-06-27
 */
@Spi("default")
public interface ListTypeHandler<T extends List> extends TypeHandler<T> {

    void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    T getNullableResult(ResultSet rs, String columnName) throws SQLException;

    T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;

    T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;
}

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
package io.github.ukuz.mybatis.type.json.main;

import io.github.ukuz.mybatis.type.json.core.AdditionalTypeHandlerRegistry;
import org.apache.ibatis.session.Configuration;

import java.net.URISyntaxException;

/**
 * @author ukuz90
 * @since 2019-06-26
 */
public class Bootstrap {

    private Configuration configuration;
    private AdditionalTypeHandlerRegistry typeHandlerRegistry;

    public Bootstrap(Configuration configuration) {
        this.configuration = configuration;
        this.typeHandlerRegistry = new AdditionalTypeHandlerRegistry();
    }

    public void scanEntityPackages(String[] scanEntityPackages) throws URISyntaxException, ClassNotFoundException {
        scanTypeHandlers(scanEntityPackages);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        registryTypeHandlers();
    }

    private void scanTypeHandlers(String[] scanEntityPackages) throws URISyntaxException, ClassNotFoundException {
        typeHandlerRegistry.startScan(scanEntityPackages);
    }

    private void registryTypeHandlers() {
        typeHandlerRegistry.setConfiguration(configuration);
    }

    public static class Builder {
        private Configuration configuration;

        public Builder configuration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Bootstrap build() {
            Bootstrap bootstrap = new Bootstrap(configuration);
            return bootstrap;
        }
    }
}

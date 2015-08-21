/*
 * Copyright 2007-2009 the original author or authors.
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
package net.paoding.rose.web.impl.mapping;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MatchResultImpl implements MatchResult {

    /** 结果字符串 */
    private String value;

    private MappingNode mappingNode;

    /** 从结果字符串中得到的资源参数名(如果该资源使用了使用了参数化的映射地址) */
    private String parameterName;

    /**
     * 创建新的匹配结果对象
     * 
     * @param value 匹配结果字符串
     */
    public MatchResultImpl(MappingNode mappingNode, String value) {
        this.mappingNode = mappingNode;
        this.value = value;
    }

    @Override
    public MappingNode getMappingNode() {
        return mappingNode;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setParameter(String name) {
        this.parameterName = name;
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public String toString() {
        return ((parameterName == null) ? getValue() : parameterName + "=" + getValue());
    }
}

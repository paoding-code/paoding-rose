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
package net.paoding.rose.web.impl.validation;

import net.paoding.rose.web.Invocation;

import org.springframework.util.Assert;
import org.springframework.validation.AbstractBindingResult;

/**
 * 控制器action方法普通参数绑定信息类，
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ParameterBindingResult extends AbstractBindingResult {

    public static final String OBJECT_NAME = "parameterBindingResult";

    private static final long serialVersionUID = -592629554361073051L;

    private transient Invocation inv;

    /**
     * 
     * @param inv
     */
    public ParameterBindingResult(Invocation inv) {
        super(OBJECT_NAME);
        Assert.notNull(inv, "Target Invocation must not be null");
        this.inv = inv;
    }

    @Override
    public Object getTarget() {
        return this.inv;
    }

    /**
     * 
     * @throws IllegalStateException 在反序列化后调用
     */
    @Override
    protected Object getActualFieldValue(String field) {
        if (inv == null) {
            throw new IllegalStateException();
        }
        return inv.getParameter(field);
    }

}

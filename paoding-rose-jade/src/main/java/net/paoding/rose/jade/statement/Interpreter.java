/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.statement;

import org.springframework.core.annotation.Order;

/**
 * 可用 {@link Order}来调节优先级，根据 {@link Order} 语义，值越小越优先，值越大越后；
 * <p>
 * 如果没有标注 {@link Order} 使用默认值0。
 * 
 * 从实践看，jade插件的解析器一般应该设置为负数，以优先于系统解析器。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
//按Spring语义规定，根据 {@link Order} 语义，值越小越优先，值越大越后；
@Order(0)
public interface Interpreter {

    /**
     * 
     * @param runtime
     */
    void interpret(StatementRuntime runtime);

}

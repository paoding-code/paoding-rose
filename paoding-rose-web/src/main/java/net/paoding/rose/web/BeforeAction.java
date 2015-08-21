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
package net.paoding.rose.web;

/**
 * 如果控制器action方法的参数对象实现了 {@link BeforeAction}接口，Rose将在调用所在action方法之前，调用
 * {@link #doBeforeAction(Invocation)}方法
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface BeforeAction {

    /**
     * 
     * @param inc
     * @param instruction
     * @throws Exception
     */
    public void doBeforeAction(Invocation inc) throws Exception;

}

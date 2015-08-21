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
package net.paoding.rose.web.var;

import java.util.Collection;
import java.util.Map;

/**
 * {@link Flash}用于声明在控制器的方法中，可以在向紧接的下一个request传递信息(特别是采用redirect的情况
 * <p>
 * 
 * 由于默认采用的是Cookie的实现，所以这个机制只能在同一个域名的下使用
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Flash {

    /**
     * 获取上一次请求传递过来的信息，如果没有返回null
     * 
     * @param name
     * @return
     */
    public String get(String name);

    /**
     * 是否传递了所给名字的信息?
     * 
     * @param name
     * @return
     */
    public boolean contains(String name);

    /**
     * 获取上一次请求传递过来的所有信息的名字，如果没有，返回的size为0
     * 
     * @return
     */
    public Collection<String> getMessageNames();

    /**
     * 获取上一次请求传递过来的所有信息，如果没有，返回的size为0
     * 
     * @return
     */
    public Map<String, String> getMessages();

    /**
     * 向下一个请求发送信息
     * 
     * @param name
     * @param flashMessage
     * @return
     */
    public Flash add(String name, String flashMessage);

    /**
     * 获取已经add进来的信息的名字
     * 
     * @return
     */
    public Collection<String> getNewMessageNames();

    /**
     * 获取已经add进来的信息
     * 
     * @return
     */
    public Map<String, String> getNewMessages();
}

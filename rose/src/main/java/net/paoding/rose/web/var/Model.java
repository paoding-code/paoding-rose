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

import java.util.Map;

/**
 * {@link Model} 代表了MVC架构中的Model角色，在View中要render某个对象的值时，
 * 在实现上需要应用程序开发者在控制器类(Controller)中 中把对象作为Model的属性加进去。
 * 
 * @see #add(Object)
 * @see #add(String, Object)
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Model {

    /**
     * 将对象(object,array,collection等)加入到MVC中的Model中作为一个属性，通过它传递给View
     * <p>
     * 将使用该对象的类名头字母小写的字符串作为名字；<br>
     * 如果对象是数组，去数组元素的类的类名字头字母小写加上"List"作为名字<br>
     * 如果对象是集合元素，取其第一个元素的类的类名字头字母小写加上"List"作为名字<br>
     * 如果该值为空或者其集合长度为0的话，将被忽略<br>
     * 
     * @param value 可以是普通对象，数组对象，集合对象；<br>
     *        可以为null，如果对象为null或集合长度为0直接被忽略掉
     * 
     */
    public Model add(Object value);

    /**
     * 将对象(object,array,collection等)加入到MVC中的Model中作为一个属性，通过它传递给View
     * 
     * @param name 在view中这个字符串将作为该对象的名字被使用；非空
     * @param value 可以是普通对象，数组对象，集合对象；<br>
     *        可以为null，如果对象为null直接被忽略掉
     */
    public Model add(String name, Object value);

    /**
     * 将modelmap的key-value照搬到 {@link Model}中来，传递给View
     * <p>
     * 不限制key一定是String类型的，但照搬过来的要通过toString()转化为String类型的key
     * 
     * @param modelMap
     * @return
     */
    public Model merge(Map<String, Object> modelMap);

    /**
     * modle中是否已经包含了这个名字的属性?
     * 
     * @param name
     * @return
     */
    public boolean contains(String name);

    /**
     * 返回指定名字的模型对象，如果没有返回null
     * 
     * @param name
     * @return
     */
    public Object get(String name);

    /**
     * 将该属性从Model中剔除掉
     * 
     * @param name
     * @return
     */
    public Model remove(String name);

    /**
     * 返回该Model中的所有属性对象(除rose内置的、和框架有关的属性)
     * 
     * @return
     */
    public Map<String, Object> getAttributes();
}

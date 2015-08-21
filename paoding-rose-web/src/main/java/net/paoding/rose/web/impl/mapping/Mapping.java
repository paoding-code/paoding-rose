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
 * 给定一个地址字符串，{@link Mapping} 能够判断其是否匹配，并返回绑定的结点。
 * <p>
 * 
 * {@link Mapping}对象是有序的，对给定的一个地址，排序在前的 {@link Mapping} 匹配成功后，整个匹配过程将中止。
 * <p>
 * 
 * {@link Mapping}对象的排序比较只和定义该映射的地址有关，和所绑定的 {@link MappingNode} 无关。不同的
 * {@link Mapping}实现都支持之间的互相比较。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Mapping extends Comparable<Mapping> {

    /**
     * 
     * 
     * @return
     */
    public String getDefinition();

    /**
     * 
     * 返回本映射绑定的结点，一个结点可能包含多个相同地址的不同资源。
     * 
     * @return
     */
    public MappingNode getMappingNode();

    /**
     * 设置本映射绑定的结点，一个结点可能包含多个相同地址的不同资源。
     * 
     * @param mappingNode
     */
    public void setMappingNode(MappingNode mappingNode);

    /**
     * 资源参数名(如果该资源使用了使用了参数化的映射地址)
     * 
     * @param name
     * @return
     */
    public String getParameterName();

    /**
     * 判断给定的请求地址<code>path</code>是否能够和本 {@link Mapping}对象相匹配。
     * <p>
     * 若能够匹配，返回非null的 {@link MatchResult}对象，并把该映射绑定的结点资源设置到匹配结果中返回。
     * <p>
     * 如果一个结点包含了多个资源，则不能具体设置哪个资源给匹配结果，这个设置将推迟到下一个匹配决定，如果下一个匹配有结果，
     * 通过下一个匹配的结点的parentResource即可得知。
     * 
     * @param path
     * @return
     */
    public MatchResult match(CharSequence path);

    /**
     * 返回该映射的地址定义以及匹配规则(比如正则表达式)
     * 
     * @return
     */
    public String toString();
}

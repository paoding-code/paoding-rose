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
package net.paoding.rose.scanner;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * 封装一个web模块资源的相关信息。rose 将每一个 controllers 包以及子包 <strong>各称为</strong>
 * 一个web模块资源。 xxx.controllers 是一个模块资源 ， xxx.controllers.subpkg
 * 是另外一个模块资源。极端情况下，不同jar文件可能具有相同的 xxx.controllers
 * 包，rose会将他们进行区分，称为2个URL地址不同的web模块资源。
 * <p>
 * rose使用web模块资源用来构造web模块。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ModuleResource implements Comparable<ModuleResource> {

    // 该模块资源的url地址，比如jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/
    private URL moduleUrl;

    // 该模块相对于xxx.controllers模块的地址
    // 如果该模块就是xxx.controllers本身，modulePath=""
    // 如果该模块是xxx.controllers.subpkg，modulePath=/subpkg
    // 和FileObject之间的相对地址有所区别：FileObject之间的相对地址是不以'/'开始的.
    private String relativePath;

    // 该模块使用的web请求地址映射，默认通过相对于上级的xxx.controllers地址modulePath确定。
    // 开发者可通过在所在模块下放置一个rose.properties文件，jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/rose.properties，
    // 配置属性module.path=/custormed定制地址映射，特别是做正则表达式配置'module.path=/${userId:[0-9]+}'
    private String mappingPath;

    // 该模块含有的spring配置文件资源，符合'applicationContext*.xml'模式的资源被称为spring配置文件，比如:
    // jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/applicationContext.xml
    // jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/applicationContext-1.xml
    // jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/applicationContext-2.xml
    private List<URL> contextResources = new LinkedList<URL>();

    // 该模块含有的messages基本名字，符合'messages*.properties'或messages*.xml'模式的资源成为messages文件
    // messages基本名字的几个例子
    // jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/messages
    // jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/messages-1
    // jar:file:/path/to/your/jarfile.jar!/xxx/controllers/subpkg/messages-2
    private List<String> messageBasenames = new LinkedList<String>();

    // 该模块含有的class类型
    private List<Class<?>> moduleClasses = new LinkedList<Class<?>>();

    // 父模块资源。xxx.controllers.subpkg的父模块资源是xxx.controllers对应的模块，xxx.controllers是顶级模块资源。
    private ModuleResource parent;

    private String[] interceptedAllow;

    private String[] interceptedDeny;

    // getters & setters

    public URL getModuleUrl() {
        return moduleUrl;
    }

    public void setModuleUrl(URL moduleUrl) {
        this.moduleUrl = moduleUrl;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public ModuleResource getParent() {
        return parent;
    }

    public void setParent(ModuleResource parent) {
        this.parent = parent;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
    }

    public List<URL> getContextResources() {
        return contextResources;
    }

    public void addContextResource(URL contextResource) {
        this.contextResources.add(contextResource);
    }

    public String[] getMessageBasenames() {
        return messageBasenames.toArray(new String[messageBasenames.size()]);
    }

    public void addMessageResource(String messageBasename) {
        this.messageBasenames.add(messageBasename);
    }

    public List<Class<?>> getModuleClasses() {
        return moduleClasses;
    }

    public void addModuleClass(Class<?> moduleClass) {
        this.moduleClasses.add(moduleClass);
    }

    public String[] getInterceptedAllow() {
        return interceptedAllow;
    }

    public void setInterceptedAllow(String[] interceptedAllow) {
        this.interceptedAllow = interceptedAllow;
    }

    public String[] getInterceptedDeny() {
        return interceptedDeny;
    }

    public void setInterceptedDeny(String[] interceptedDeny) {
        this.interceptedDeny = interceptedDeny;
    }

    // -- overrides --

    /**
     * 使父级别的模块资源比子模块资源比较起来更小
     */
    @Override
    public int compareTo(ModuleResource o) {
        return (moduleUrl.toString().length() - o.moduleUrl.toString().length());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModuleResource)) {
            return false;
        }
        return this.moduleUrl.equals(((ModuleResource) obj).moduleUrl);
    }

    @Override
    public int hashCode() {
        return this.moduleUrl.hashCode() * 13;
    }

    @Override
    public String toString() {
        return "ModuleResource[" + mappingPath + "=" + moduleUrl + "] " //
                + "size of (class, ctx, messagesBasename) = (" + moduleClasses.size() //
                + ", " + contextResources.size() + ", " + messageBasenames.size() + ")"//
        ;
    }
}

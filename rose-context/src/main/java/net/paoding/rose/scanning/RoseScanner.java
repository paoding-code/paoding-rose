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
package net.paoding.rose.scanning;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * @author zhiliang.wang 王志亮 [qieqie.wang@gmail.com]
 */
public class RoseScanner {

    private static SoftReference<RoseScanner> softReference;

    public synchronized static RoseScanner getInstance() {
        if (softReference == null || softReference.get() == null) {
            RoseScanner roseScanner = new RoseScanner();
            softReference = new SoftReference<RoseScanner>(roseScanner);
        }
        return softReference.get();
    }

    // -------------------------------------------------------------

    protected Log logger = LogFactory.getLog(getClass());

    protected Date createTime = new Date();

    protected ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private List<ResourceRef> classesFolderResources;

    private List<ResourceRef> jarResources;

    // -------------------------------------------------------------

    private RoseScanner() {
    }

    public Date getCreateTime() {
        return createTime;
    }

    // -------------------------------------------------------------
    public List<ResourceRef> getJarOrClassesFolderResources() throws IOException {
        return getJarOrClassesFolderResources(null);
    }

    public List<ResourceRef> getJarOrClassesFolderResources(String[] scope) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("[findFiles] start to found classes folders "
                    + "or rosed jar files by scope:" + Arrays.toString(scope));
        }
        List<ResourceRef> resources;
        if (scope == null) {
            resources = new LinkedList<ResourceRef>();
            if (logger.isDebugEnabled()) {
                logger.debug("[findFiles] call 'classesFolder'");
            }
            resources.addAll(getClassesFolderResources());
            //
            if (logger.isDebugEnabled()) {
                logger.debug("[findFiles] exits from 'classesFolder'");
                logger.debug("[findFiles] call 'jarFile'");
            }
            resources.addAll(getJarResources());
            if (logger.isDebugEnabled()) {
                logger.debug("[findFiles] exits from 'jarFile'");
            }
        } else if (scope.length == 0) {
            return new ArrayList<ResourceRef>();
        } else {
            resources = new LinkedList<ResourceRef>();
            for (String scopeEntry : scope) {
                String packagePath = scopeEntry.replace('.', '/');
                Resource[] packageResources = resourcePatternResolver.getResources("classpath*:"
                        + packagePath);
                for (Resource pkgResource : packageResources) {
                    String uri = pkgResource.getURI().toString();
                    uri = StringUtils.removeEnd(uri, "/");
                    packagePath = StringUtils.removeEnd(packagePath, "/");
                    uri = StringUtils.removeEnd(uri, packagePath);
                    int beginIndex = uri.lastIndexOf("file:");
                    if (beginIndex == -1) {
                        beginIndex = 0;
                    } else {
                        beginIndex += "file:".length();
                    }
                    int endIndex = uri.lastIndexOf('!');
                    if (endIndex == -1) {
                        endIndex = uri.length();
                    }
                    String path = uri.substring(beginIndex, endIndex);
                    Resource folder = new FileSystemResource(path);
                    ResourceRef ref = ResourceRef.toResourceRef(folder);
                    if (!resources.contains(ref)) {
                        resources.add(ref);
                        if (logger.isDebugEnabled()) {
                            logger.debug("[findFiles] found classes folders "
                                    + "or rosed jar files by scope:" + ref);
                        }
                    }
                }
            }
        }
        //
        if (logger.isInfoEnabled()) {
            logger.info("[findFiles] found " + resources.size() + " classes folders "
                    + "or rosed jar files : " + resources);
        }

        return resources;
    }

    /**
     * 将要被扫描的普通类地址(比如WEB-INF/classes或target/classes之类的地址)
     * 
     * @param resourceLoader
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ResourceRef> getClassesFolderResources() throws IOException {
        if (classesFolderResources == null) {
            if (logger.isInfoEnabled()) {
                logger.info("[classesFolder] start to found available classes folders ...");
            }
            List<ResourceRef> classesFolderResources = new ArrayList<ResourceRef>();
            Enumeration<URL> founds = resourcePatternResolver.getClassLoader().getResources("");
            while (founds.hasMoreElements()) {
                URL urlObject = founds.nextElement();
                if (!"file".equals(urlObject.getProtocol())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[classesFolder] Ignored classes folder because "
                                + "not a file protocol url: " + urlObject);
                    }
                    continue;
                }
                String path = urlObject.getPath();
                Assert.isTrue(path.endsWith("/"));
//                if (!path.endsWith("/classes/") && !path.endsWith("/bin/")) {
//                    if (logger.isInfoEnabled()) {
//                        logger.info("[classesFolder] Ignored classes folder because "
//                                + "not ends with '/classes/' or '/bin/': " + urlObject);
//                    }
//                    continue;
//                }
                File file;
                try {
                    file = new File(urlObject.toURI());
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
                if (file.isFile()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[classesFolder] Ignored because not a directory: "
                                + urlObject);
                    }
                    continue;
                }
                Resource resource = new FileSystemResource(file);
                ResourceRef resourceRef = ResourceRef.toResourceRef(resource);
                if (classesFolderResources.contains(resourceRef)) {
                    // 删除重复的地址
                    if (logger.isDebugEnabled()) {
                        logger.debug("[classesFolder] remove replicated classes folder: "
                                + resourceRef);
                    }
                } else {
                    classesFolderResources.add(resourceRef);
                    if (logger.isDebugEnabled()) {
                        logger.debug("[classesFolder] add classes folder: " + resourceRef);
                    }
                }
            }
            // 删除含有一个地址包含另外一个地址的
            Collections.sort(classesFolderResources);
            List<ResourceRef> toRemove = new LinkedList<ResourceRef>();
            for (int i = 0; i < classesFolderResources.size(); i++) {
                ResourceRef ref = classesFolderResources.get(i);
                String refURI = ref.getResource().getURI().toString();
                for (int j = i + 1; j < classesFolderResources.size(); j++) {
                    ResourceRef refj = classesFolderResources.get(j);
                    String refjURI = refj.getResource().getURI().toString();
                    if (refURI.startsWith(refjURI)) {
                        toRemove.add(refj);
                        if (logger.isInfoEnabled()) {
                            logger.info("[classesFolder] remove wrapper classes folder: " //
                                    + refj);
                        }
                    } else if (refjURI.startsWith(refURI) && refURI.length() != refjURI.length()) {
                        toRemove.add(ref);
                        if (logger.isInfoEnabled()) {
                            logger.info("[classesFolder] remove wrapper classes folder: " //
                                    + ref);
                        }
                    }
                }
            }
            classesFolderResources.removeAll(toRemove);
            //
            this.classesFolderResources = new ArrayList<ResourceRef>(classesFolderResources);
            if (logger.isInfoEnabled()) {
                logger.info("[classesFolder] found " + classesFolderResources.size()
                        + " classes folders: " + classesFolderResources);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[classesFolder] found cached " + classesFolderResources.size()
                        + " classes folders: " + classesFolderResources);
            }
        }
        return Collections.unmodifiableList(classesFolderResources);
    }

    /**
     * 将要被扫描的jar资源
     * 
     * @param resourceLoader
     * @return
     * @throws IOException
     */
    public List<ResourceRef> getJarResources() throws IOException {
        if (jarResources == null) {
            if (logger.isInfoEnabled()) {
                logger.info("[jarFile] start to found available jar files for rose to scanning...");
            }
            List<ResourceRef> jarResources = new LinkedList<ResourceRef>();
            Resource[] metaInfResources = resourcePatternResolver
                    .getResources("classpath*:/META-INF/");
            for (Resource metaInfResource : metaInfResources) {
                URL urlObject = metaInfResource.getURL();
                if (ResourceUtils.isJarURL(urlObject)) {
                    try {
                        String path = URLDecoder.decode(urlObject.getPath(), "UTF-8"); // fix 20%
                        if (path.startsWith("file:")) {
                            path = path.substring("file:".length(), path.lastIndexOf('!'));
                        } else {
                            path = path.substring(0, path.lastIndexOf('!'));
                        }
                        Resource resource = new FileSystemResource(path);
                        if (jarResources.contains(resource)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("[jarFile] skip replicated jar resource: " + path);// 在多个 linux环境 下发现有重复,fix it!
                            }
                        } else {
                            ResourceRef ref = ResourceRef.toResourceRef(resource);
                            if (ref.getModifiers() != null) {
                                jarResources.add(ref);
                                if (logger.isInfoEnabled()) {
                                    logger.info("[jarFile] add jar resource: " + ref);
                                }
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("[jarFile] not rose jar resource: " + path);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(urlObject, e);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[jarFile] not rose type(not a jar) " + urlObject);
                    }
                }
            }
            this.jarResources = jarResources;
            if (logger.isInfoEnabled()) {
                logger.info("[jarFile] found " + jarResources.size() + " jar files: "
                        + jarResources);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[jarFile] found cached " + jarResources.size() + " jar files: "
                        + jarResources);
            }
        }
        return Collections.unmodifiableList(jarResources);
    }

}

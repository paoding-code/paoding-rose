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
package net.paoding.rose.jade.context.spring;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.paoding.rose.jade.annotation.DAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

/**
 * {@link JadeComponentProvider}用于查找一个目录或jar包下符合Jade规范的DAO接口。
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 * @see JadeComponentProvider
 * @see org.springframework.core.type.classreading.MetadataReaderFactory
 * @see org.springframework.core.type.AnnotationMetadata
 * @see ScannedGenericBeanDefinition
 */
public class JadeComponentProvider implements ResourceLoaderAware {

    /**
     * 日志记录器
     */
    private final Log logger = LogFactory.getLog(JadeComponentProvider.class);

    /**
     * 
     */
    private String resourcePattern = "**/*DAO.class";

    /**
     * 
     */
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * 
     */
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
            resourcePatternResolver);

    /**
     * 
     */
    private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();

    /**
     * 
     */
    private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();

    /**
     * 
     */
    public JadeComponentProvider() {
        includeFilters.add(new AnnotationTypeFilter(DAO.class));
    }

    /**
     * Set the ResourceLoader to use for resource locations. This will
     * typically be a ResourcePatternResolver implementation.
     * <p>
     * Default is PathMatchingResourcePatternResolver, also capable of
     * resource pattern resolving through the ResourcePatternResolver
     * interface.
     * 
     * @see org.springframework.core.io.support.ResourcePatternResolver
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils
                .getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    /**
     * Return the ResourceLoader that this component provider uses.
     */
    public final ResourceLoader getResourceLoader() {
        return this.resourcePatternResolver;
    }

    /**
     * Set the resource pattern to use when scanning the classpath. This
     * value will be appended to each base package name.
     * 
     * @see #findCandidateComponents(String)
     */
    public void setResourcePattern(String resourcePattern) {
        Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
        this.resourcePattern = resourcePattern;
    }

    /**
     * Add an exclude type filter to the <i>front</i> of the exclusion
     * list.
     */
    public void addExcludeFilter(TypeFilter excludeFilter) {
        this.excludeFilters.add(0, excludeFilter);
    }

    /**
     * 查找并返回一个目录或jar包下符合Jade规范的DAO接口。
     * <p>
     * 所返回的每一个BeanDefinition代表一个符合规范的DAO接口，我们可以通过
     * {@link BeanDefinition#getBeanClassName()} 得到对应的DAO接口的类名
     * <p>
     * 所返回的BeanDefinition代表的是一个接口，不能直接注册到Spring容器中，必须先做额外的转化！
     */
    public Set<BeanDefinition> findCandidateComponents(String uriPrefix) {
        if (!uriPrefix.endsWith("/")) {
            uriPrefix = uriPrefix + "/";
        }
        Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
        try {
            String packageSearchPath = uriPrefix + this.resourcePattern;
            boolean traceEnabled = logger.isDebugEnabled();
            boolean debugEnabled = logger.isDebugEnabled();
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            if (debugEnabled) {
                logger.debug("[jade/find] find " + resources.length + " resources for "
                        + packageSearchPath);
            }
            for (int i = 0; i < resources.length; i++) {
                Resource resource = resources[i];
                if (traceEnabled) {
                    logger.trace("[jade/find] scanning " + resource);
                }
                // resourcePatternResolver.getResources出来的classPathResources，metadataReader对其进行getInputStream的时候为什么返回null呢？
                // 不得不做一个exists判断
                if (!resource.exists()) {
                    if (debugEnabled) {
                        logger.debug("Ignored because not exists:" + resource);
                    }
                } else if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory
                            .getMetadataReader(resource);
                    if (isCandidateComponent(metadataReader)) {
                        ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(
                                metadataReader);
                        sbd.setResource(resource);
                        sbd.setSource(resource);
                        if (sbd.getMetadata().isInterface() && sbd.getMetadata().isIndependent()) {
                            if (debugEnabled) {
                                logger.debug("Identified candidate component class: " + resource);
                            }
                            candidates.add(sbd);
                        } else {
                            if (traceEnabled) {
                                logger.trace("Ignored because not a interface top-level class: "
                                        + resource);
                            }
                        }
                    } else {
                        if (traceEnabled) {
                            logger.trace("Ignored because not matching any filter: " + resource);
                        }
                    }
                } else {
                    if (traceEnabled) {
                        logger.trace("Ignored because not readable: " + resource);
                    }
                }
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during jade scanning", ex);
        }
        return candidates;
    }

    /**
     * Determine whether the given class does not match any exclude filter
     * and does match at least one include filter.
     * 
     * @param metadataReader the ASM ClassReader for the class
     * @return whether the class qualifies as a candidate component
     */
    protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
        for (TypeFilter tf : this.excludeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return false;
            }
        }
        for (TypeFilter tf : this.includeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return true;
            }
        }
        return false;
    }

}

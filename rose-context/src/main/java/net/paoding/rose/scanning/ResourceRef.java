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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * 
 * 
 * @author zhiliang.wang 王志亮 [qieqie.wang@gmail.com]
 */
public class ResourceRef implements Comparable<ResourceRef> {

    private static final Log logger = LogFactory.getLog(ResourceRef.class);

    private Properties properties = new Properties();

    private Resource resource;

    private String[] modifiers;

    public static ResourceRef toResourceRef(Resource folder) throws IOException {
        ResourceRef rr = new ResourceRef(folder, null, null);
        String[] modifiers = null;
        Resource rosePropertiesResource = rr.getInnerResource("META-INF/rose.properties");
        if (rosePropertiesResource.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("found rose.properties: " + rosePropertiesResource.getURI());
            }
            InputStream in = rosePropertiesResource.getInputStream();
            rr.properties.load(in);
            in.close();
            String attrValue = rr.properties.getProperty("rose");
            if (attrValue == null) {
                attrValue = rr.properties.getProperty("Rose");
            }
            if (attrValue != null) {
                modifiers = StringUtils.split(attrValue, ", ;\n\r\t");
                if (logger.isDebugEnabled()) {
                    logger.debug("modifiers[by properties][" + rr.getResource().getURI() + "]="
                            + Arrays.toString(modifiers));
                }
            }
        }
        //
        if (modifiers == null) {
            if (!"jar".equals(rr.getProtocol())) {
                modifiers = new String[] { "**" };
                if (logger.isDebugEnabled()) {
                    logger.debug("modifiers[by default][" + rr.getResource().getURI() + "]="
                            + Arrays.toString(modifiers));
                }
            } else {
                JarFile jarFile = new JarFile(rr.getResource().getFile());
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    Attributes attributes = manifest.getMainAttributes();
                    String attrValue = attributes.getValue("rose");
                    if (attrValue == null) {
                        attrValue = attributes.getValue("Rose");
                    }
                    if (attrValue != null) {
                        modifiers = StringUtils.split(attrValue, ", ;\n\r\t");
                        if (logger.isDebugEnabled()) {
                            logger.debug("modifiers[by manifest.mf][" + rr.getResource().getURI()
                                    + "]=" + Arrays.toString(modifiers));
                        }
                    }
                }
            }
        }
        rr.setModifiers(modifiers);
        return rr;
    }

    public ResourceRef(Resource resource, String[] modifiers, Properties p) {
        setResource(resource);
        if (modifiers != null) {
            setModifiers(modifiers);
        }
        if (p != null) {
            properties.putAll(p);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public String[] getModifiers() {
        return modifiers;
    }

    public void setModifiers(String[] modifiers) {
        this.modifiers = modifiers;
        if (modifiers == null) {
            properties.remove("rose");
        } else {
            StringBuilder sb = new StringBuilder();
            final String separator = ", ";
            for (String m : modifiers) {
                sb.append(m).append(separator);
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - separator.length());
            }
            properties.put("rose", sb.toString());
        }
    }

    public boolean hasModifier(String modifier) {
        if (modifier.startsWith("<") && modifier.endsWith(">")) {
            return ArrayUtils.contains(modifiers, modifier.substring(1, modifier.length() - 1));
        }
        return ArrayUtils.contains(modifiers, "**") || ArrayUtils.contains(modifiers, "*")
                || ArrayUtils.contains(modifiers, modifier);
    }

    public Resource getInnerResource(String subPath) throws IOException {
        Assert.isTrue(!subPath.startsWith("/"));
        String rootPath = resource.getURI().getPath();
        if (getProtocol().equals("jar")) {
            return new UrlResource("jar:file:" + rootPath + "!/" + subPath);
        } else {
            return new FileSystemResource(rootPath + subPath); // 已使用FileSystemResource不用file:打头
        }
    }

    public Resource[] getInnerResources(ResourcePatternResolver resourcePatternResolver,
            String subPath) throws IOException {
        subPath = getInnerResourcePattern(subPath);
        return resourcePatternResolver.getResources(subPath);
    }

    public String getInnerResourcePattern(String subPath) throws IOException {
        Assert.isTrue(!subPath.startsWith("/"), subPath);
        String rootPath = resource.getURI().getPath();
        if (getProtocol().equals("jar")) {
            subPath = "jar:file:" + rootPath + ResourceUtils.JAR_URL_SEPARATOR + subPath;
        } else {
            subPath = "file:" + rootPath + subPath;
        }
        return subPath;
    }

    public String getProtocol() {
        if (resource.getFilename().toLowerCase().endsWith(".jar")
                || resource.getFilename().toLowerCase().endsWith(".zip")
                || resource.getFilename().toLowerCase().endsWith(".tar")
                || resource.getFilename().toLowerCase().endsWith(".gz")) {
            return "jar";
        }
        return "file";
    }

    @Override
    public int compareTo(ResourceRef o) {
        try {
            return this.resource.getURI().compareTo(o.resource.getURI());
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Override
    public int hashCode() {
        return 13 * resource.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (resource == null) return false;
        if (obj instanceof Resource) {
            return resource.equals(obj);
        } else if (obj instanceof ResourceRef) {
            return resource.equals(((ResourceRef) obj).resource);
        }
        return false;
    }

    @Override
    public String toString() {
        String[] modifiers = this.modifiers;
        if (modifiers == null) {
            modifiers = new String[0];
        }
        try {
            return resource.getURL().getFile() + Arrays.toString(modifiers);
        } catch (IOException e) {
            return resource + Arrays.toString(modifiers);
        }
    }
}

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

import static net.paoding.rose.RoseConstants.CONF_INTERCEPTED_ALLOW;
import static net.paoding.rose.RoseConstants.CONF_INTERCEPTED_DENY;
import static net.paoding.rose.RoseConstants.CONF_MODULE_IGNORED;
import static net.paoding.rose.RoseConstants.CONF_MODULE_PATH;
import static net.paoding.rose.RoseConstants.CONF_PARENT_MODULE_PATH;
import static net.paoding.rose.RoseConstants.CONTROLLERS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.paoding.rose.scanning.LoadScope;
import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;
import net.paoding.rose.scanning.vfs.FileName;
import net.paoding.rose.scanning.vfs.FileObject;
import net.paoding.rose.scanning.vfs.FileSystemManager;
import net.paoding.rose.util.RoseStringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ModuleResourceProviderImpl implements ModuleResourceProvider {

    private Log logger = LogFactory.getLog(ModuleResourceProviderImpl.class);

    class Local {

        List<ModuleResource> moduleResourceList = new LinkedList<ModuleResource>();

        Map<FileObject, ModuleResource> moduleResourceMap = new HashMap<FileObject, ModuleResource>();
    }

    @Override
    public List<ModuleResource> findModuleResources(LoadScope scope) throws IOException {

        Local local = new Local();
        String[] controllersScope = scope.getScope("controllers");
        if (logger.isInfoEnabled()) {
            logger.info("[moduleResource] starting ...");
            logger.info("[moduleResource] call 'findFiles':"
                    + " to find classes or jar files by scope "//
                    + Arrays.toString(controllersScope));
        }

        List<ResourceRef> refers = RoseScanner.getInstance().getJarOrClassesFolderResources(
                controllersScope);

        if (logger.isInfoEnabled()) {
            logger.info("[moduleResource] exits from 'findFiles'");
            logger.info("[moduleResource] going to scan controllers"
                    + " from these folders or jar files:" + refers);
        }

        FileSystemManager fileSystem = new FileSystemManager();

        for (ResourceRef refer : refers) {
            Resource resource = refer.getResource();
            if (!refer.hasModifier("controllers")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[moduleResource] Ignored because not marked as 'controllers'"
                            + " in META-INF/rose.properties or META-INF/MANIFEST.MF: "
                            + resource.getURI());
                }
                continue;
            }
            File resourceFile = resource.getFile();
            String urlString;
            if ("jar".equals(refer.getProtocol())) {
                urlString = ResourceUtils.URL_PROTOCOL_JAR + ":" + resourceFile.toURI()
                        + ResourceUtils.JAR_URL_SEPARATOR;
            } else {
                urlString = resourceFile.toURI().toString();
            }
            FileObject rootObject = fileSystem.resolveFile(urlString);
            if (rootObject == null || !rootObject.exists()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[moduleResource] Ignored because not exists: " + urlString);
                }
                continue;
            }

            if (logger.isInfoEnabled()) {
                logger.info("[moduleResource] start to scan moduleResource in file: " + rootObject);
            }

            try {
                int oldSize = local.moduleResourceList.size();

                deepScanImpl(local, rootObject, rootObject);

                int newSize = local.moduleResourceList.size();

                if (logger.isInfoEnabled()) {
                    logger.info("[moduleResource] got " + (newSize - oldSize) + " modules in "
                            + rootObject);
                }

            } catch (Exception e) {
                logger.error("[moduleResource] error happend when scanning " + rootObject, e);
            }

            fileSystem.clearCache();
        }

        afterScanning(local);

        logger.info("[moduleResource] found " + local.moduleResourceList.size()
                + " module resources ");

        return local.moduleResourceList;
    }

    protected void deepScanImpl(Local local, FileObject root, FileObject target) throws IOException {
        if (CONTROLLERS.equals(target.getName().getBaseName())) {
            checkModuleResourceCandidate(local, root, target, target);
        } else {
            FileObject[] children = target.getChildren();
            for (FileObject child : children) {
                if (child.getType().hasChildren()) {
                    deepScanImpl(local, root, child);
                }
            }
        }
    }

    protected void checkModuleResourceCandidate(Local local, FileObject root,
            FileObject topModuleFile, FileObject candidate) throws IOException {

        String relative = topModuleFile.getName().getRelativeName(candidate.getName());
        String mappingPath = null;
        String[] interceptedAllow = null;
        String[] interceptedDeny = null;

        ModuleResource parentModule = local.moduleResourceMap.get(candidate.getParent());
        // 如果rose.properties设置了controllers的module.path?
        FileObject rosePropertiesFile = candidate.getChild("rose.properties");
        if (rosePropertiesFile != null && rosePropertiesFile.exists()) {
            Properties p = new Properties();
            InputStream in = rosePropertiesFile.getContent().getInputStream();
            p.load(in);
            in.close();

            // 如果controllers=ignored，则...
            String ignored = p.getProperty(CONF_MODULE_IGNORED, "false").trim();
            if ("true".equalsIgnoreCase(ignored) || "1".equalsIgnoreCase(ignored)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Ignored module(include submodules) by rose.properties[ignored="
                            + ignored + "]: " + candidate);
                }
                return;
            }

            mappingPath = p.getProperty(CONF_MODULE_PATH);
            if (mappingPath != null) {
                mappingPath = mappingPath.trim();
                String parentModulePlaceHolder = "${" + CONF_PARENT_MODULE_PATH + "}";
                if (mappingPath.indexOf(parentModulePlaceHolder) != -1) {
                    String parentModulePath = "";
                    if (candidate.getParent() != null) {
                        parentModulePath = (parentModule == null) ? "" : parentModule
                                .getMappingPath();
                    }
                    mappingPath = mappingPath.replace(parentModulePlaceHolder, parentModulePath);
                }
                if (mappingPath.length() != 0 && !mappingPath.startsWith("/")) {
                    if (parentModule != null) {
                        mappingPath = parentModule.getMappingPath() + "/" + mappingPath;
                    } else if (StringUtils.isNotEmpty(relative)) {
                        mappingPath = relative + "/" + mappingPath;
                    } else {
                        mappingPath = "/" + mappingPath;
                    }
                }
                mappingPath = RoseStringUtil.mappingPath(mappingPath);
            }

            //interceptedAllow、interceptedDeny
            String interceptedAllowStrings = p.getProperty(CONF_INTERCEPTED_ALLOW);
            interceptedAllowStrings = StringUtils.trimToEmpty(interceptedAllowStrings);
            if (interceptedAllowStrings.length() > 0) {
                interceptedAllow = StringUtils.split(interceptedAllowStrings, ",");
            }

            String interceptedDenyStrings = p.getProperty(CONF_INTERCEPTED_DENY);
            interceptedDenyStrings = StringUtils.trimToEmpty(interceptedDenyStrings);
            if (interceptedDenyStrings.length() > 0) {
                interceptedDeny = StringUtils.split(interceptedDenyStrings, ",");
            }

        }
        // 
        if (mappingPath == null) {
            if (parentModule != null) {
                mappingPath = parentModule.getMappingPath() + "/"
                        + candidate.getName().getBaseName();
            } else {
                mappingPath = "";
            }
        }
        ModuleResource moduleResource = new ModuleResource();
        moduleResource.setMappingPath(mappingPath);
        moduleResource.setModuleUrl(candidate.getURL());
        moduleResource.setRelativePath(RoseStringUtil.relativePathToModulePath(relative));
        moduleResource.setParent(parentModule);
        if (interceptedAllow != null) {
            moduleResource.setInterceptedAllow(interceptedAllow);
        }
        if (interceptedDeny != null) {
            moduleResource.setInterceptedDeny(interceptedDeny);
        }
        local.moduleResourceMap.put(candidate, moduleResource);
        local.moduleResourceList.add(moduleResource);
        if (logger.isDebugEnabled()) {
            logger.debug("found module '" + mappingPath + "' in " + candidate.getURL());
        }

        FileObject[] children = candidate.getChildren();
        for (FileObject child : children) {
            if (child.getType().hasContent() && !child.getType().hasChildren()) {
                handlerModuleResource(local, root, candidate, child);
            }
        }
        for (FileObject child : children) {
            if (child.getType().hasChildren()) {
                checkModuleResourceCandidate(local, root, topModuleFile, child);
            }
        }
    }

    protected void handlerModuleResource(Local local, FileObject rootObject, FileObject thisFolder,
            FileObject resource) throws IOException {
        FileName fileName = resource.getName();
        String bn = fileName.getBaseName();
        if (logger.isDebugEnabled()) {
            logger.debug("handlerModuleResource baseName=" + bn + "; file="
                    + fileName.getFileObject());
        }
        if (bn.endsWith(".class") && bn.indexOf('$') == -1) {
            addModuleClass(local, rootObject, thisFolder, resource);
        } else if (bn.startsWith("applicationContext") && bn.endsWith(".xml")) {
            addModuleContext(local, rootObject, thisFolder, resource);
        } else if (bn.startsWith("messages") && (bn.endsWith(".xml") || bn.endsWith(".properties"))) {
            addModuleMessage(local, rootObject, thisFolder, resource);
        }
    }

    private void addModuleContext(Local local, FileObject rootObject, FileObject thisFolder,
            FileObject resource) throws IOException {
        ModuleResource moduleInfo = local.moduleResourceMap.get(thisFolder);
        moduleInfo.addContextResource(resource.getURL());
        if (logger.isDebugEnabled()) {
            logger.debug("module '" + moduleInfo.getMappingPath() + "': found context file, url="
                    + resource.getURL());
        }
    }

    private void addModuleMessage(Local local, FileObject rootObject, FileObject thisFolder,
            FileObject resource) throws IOException {
        ModuleResource moduleInfo = local.moduleResourceMap.get(thisFolder);
        String directory = resource.getParent().getURL().toString();
        String messageFileName = resource.getName().getBaseName();
        String msgBasename;
        if (messageFileName.indexOf('_') == -1) {
            msgBasename = messageFileName.substring(0, messageFileName.indexOf('.'));
        } else {
            msgBasename = messageFileName.substring(0, messageFileName.indexOf('_'));
        }
        moduleInfo.addMessageResource(directory + msgBasename);
        if (logger.isDebugEnabled()) {
            logger.debug("module '" + moduleInfo.getMappingPath() + "': found messages file, url="
                    + resource.getURL());
        }
    }

    private void addModuleClass(Local local, FileObject rootObject, FileObject thisFolder,
            FileObject resource) throws IOException {
        String className = rootObject.getName().getRelativeName(resource.getName());
        Assert.isTrue(!className.startsWith("/"));
        className = StringUtils.removeEnd(className, ".class");
        className = className.replace('/', '.');
        ModuleResource module = local.moduleResourceMap.get(thisFolder);
        try {
            // TODO: classloader...
            module.addModuleClass(Class.forName(className));
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': found class, name="
                        + className);
            }
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
    }

    // FIXME: 如果一个module只有rose.properties文件也会从moduleInfoList中remove，以后是否需要修改？
    protected void afterScanning(Local local) {
        for (ModuleResource moduleResource : local.moduleResourceMap.values()) {
            if (moduleResource.getContextResources().size() == 0
                    && moduleResource.getModuleClasses().size() == 0) {
                local.moduleResourceList.remove(moduleResource);
                if (logger.isInfoEnabled()) {
                    logger.info("remove empty module '" + moduleResource.getMappingPath() + "' "
                            + moduleResource.getModuleUrl());
                }
            }
        }
    }

}

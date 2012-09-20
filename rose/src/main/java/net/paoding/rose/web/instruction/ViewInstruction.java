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
package net.paoding.rose.web.instruction;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.util.SpringUtils;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.view.ViewDispatcher;
import net.paoding.rose.web.impl.view.ViewDispatcherImpl;
import net.paoding.rose.web.impl.view.ViewPathCache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * {@link ViewInstruction} 实现 {@link Instruction}接口，调用 {@link ViewResolver}
 * 渲染页面
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ViewInstruction extends AbstractInstruction {

    protected static Log logger = LogFactory.getLog(ViewInstruction.class);

    public static final String ROSE_INVOCATION = "roseInvocation";

    // 视图名称到视图地址的映射(缓存这个映射避免重复计算视图地址)
    private static Map<String, ViewPathCache> globalViewPathCaches = new HashMap<String, ViewPathCache>();

    // 视图名称，不包含路径，一般没有后缀名
    private final String name;

    // 如果applicationContext能够获取到这个名字的对象，则使用这个对象作为viewResolver
    private String viewDispatcherName = "viewDispatcher";

    public ViewInstruction(String name) {
        this.name = name;
    }

    @Override
    public void doRender(Invocation inv) throws Exception {
        String name = resolvePlaceHolder(this.name, inv);
        ViewDispatcher viewResolver = getViewDispatcher(inv);
        String viewPath = getViewPath((InvocationBean) inv, name);
        if (viewPath != null) {
            HttpServletRequest request = inv.getRequest();
            HttpServletResponse response = inv.getResponse();
            //
            View view = viewResolver.resolveViewName(inv, viewPath, request.getLocale());

            if (!Thread.interrupted()) {
                inv.addModel(ROSE_INVOCATION, inv);
                view.render(inv.getModel().getAttributes(), request, response);
            } else {
                logger.info("interrupted");
            }
        }
    }

    /**
     * 
     * @param inv
     * @param viewName 大多数情况viewName应该是一个普通字符串 (e.g:
     *        index)，也可能是index.jsp带后缀的字符串，
     *        可能是一个带有/开头的绝对路径地址，可能是类似template/default这样的地址
     * @return
     * @throws IOException
     */
    private String getViewPath(InvocationBean inv, String viewName) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("resolving view name = '" + viewName + "'");
        }
        // 如果以/开头、非/views/的表示到绝对路径的文件
        if (viewName.charAt(0) == '/'
                && !viewName.startsWith(RoseConstants.VIEWS_PATH_WITH_END_SEP)) {
            return viewName;
        }
        // 其他的按惯例行走
        String viewRelativePath;
        if (viewName.startsWith(RoseConstants.VIEWS_PATH_WITH_END_SEP)) {
            viewRelativePath = "";
            viewName = viewName.substring(RoseConstants.VIEWS_PATH_WITH_END_SEP.length());
        } else {
            viewRelativePath = inv.getViewModule().getRelativePath();
        }
        ViewPathCache viewPathCache = globalViewPathCaches.get(viewRelativePath);
        if (viewPathCache == null) {
            String directoryPath = RoseConstants.VIEWS_PATH + viewRelativePath;
            File directoryFile = new File(inv.getServletContext().getRealPath(directoryPath));
            if (!directoryFile.exists()) {
                String msg = "404: view directory not found, you need to create it in your webapp:"
                        + directoryPath;
                logger.error(msg);
                inv.getResponse().sendError(404, msg);
                return null;
            }
            viewPathCache = new ViewPathCache(viewRelativePath);
            globalViewPathCaches.put(viewRelativePath, viewPathCache);
        }
        //
        String viewPath;
        int queryStringIndex = viewName.indexOf('?');
        if (queryStringIndex < 0) {
            viewPath = getViewPathFromCache(inv, viewPathCache, viewName);
        } else {
            viewPath = getViewPathFromCache(inv, viewPathCache, viewName.substring(0,
                    queryStringIndex))
                    + viewName.substring(queryStringIndex);
        }

        if (viewPath != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("found '" + viewPath + "' for viewName '" + viewName + "'");
            }
        }
        return viewPath;
    }

    /**
     * 
     * @param inv
     * @param viewPathCache
     * @param viewName
     * @return
     * @throws IOException
     */
    private String getViewPathFromCache(InvocationBean inv, ViewPathCache viewPathCache,
            final String viewName) throws IOException {
        String viewPath = viewPathCache.getViewPath(viewName);
        if (viewPath != null) {
            return viewPath;
        }

        final boolean debugEnabled;
        if (debugEnabled = logger.isDebugEnabled()) {
            logger.debug("to find viewPath by viewName '" + viewName + "'");
        }
        //
        final String notDirectoryViewName;
        File directoryFile;
        String directoryPath;
        //
        int viewNameIndex = viewName.lastIndexOf('/');
        if (viewNameIndex > 0) {
            notDirectoryViewName = viewName.substring(viewNameIndex + 1);

            if (viewName.charAt(0) == '/') {
                directoryPath = viewName.substring(0, viewNameIndex);
                directoryFile = new File(inv.getServletContext().getRealPath(directoryPath));
            } else {
                directoryPath = viewPathCache.getDirectoryPath();
                String subDirPath = viewName.substring(0, viewNameIndex);
                File tempHome = new File(inv.getServletContext().getRealPath(directoryPath));
                if (!tempHome.exists()) {
                    directoryFile = null;
                } else {
                    directoryFile = searchDirectory(tempHome, subDirPath);
                    if (directoryFile != null) {
                        subDirPath = directoryFile.getPath().substring(tempHome.getPath().length())
                                .replace('\\', '/');
                        directoryPath = directoryPath + subDirPath;
                    }
                }
            }
        } else {
            directoryPath = viewPathCache.getDirectoryPath();
            notDirectoryViewName = viewName;
            directoryFile = new File(inv.getServletContext().getRealPath(directoryPath));
        }
        if (directoryFile == null || !directoryFile.exists()) {
            logger.error("not found directoryPath '" + directoryPath + "' for directoryFile '"
                    + directoryFile + "' of view named '" + viewName + "'");
            inv.getResponse().sendError(404, "not found directoryPath '" + directoryPath + "'");
            return null;
        } else {
            if (debugEnabled) {
                logger.debug("found directory " + directoryFile.getAbsolutePath());
            }
            String viewFileName = searchViewFile(directoryFile, notDirectoryViewName, false);
            if (viewFileName == null) {
                viewFileName = searchViewFile(directoryFile, notDirectoryViewName, true);
            }
            if (viewFileName == null) {
                String msg = "not found view file '" + notDirectoryViewName + "' in "
                        + directoryPath;
                if (logger.isWarnEnabled()) {
                    logger.warn(msg);
                }
                inv.getResponse().sendError(404, msg);
                return null;
            } else {
                viewPath = directoryPath + "/" + viewFileName;
                viewPathCache.setViewPath(viewName, viewPath);
                return viewPath;
            }
        }
    }

    /**
     * 优先获取大小写敏感的路径，如若找不到，则获取忽略大小写后的路径
     * 
     * @param tempHome
     * @param subDirPath
     * @return
     */
    private File searchDirectory(File tempHome, String subDirPath) {
        // 
        String[] subDirs = StringUtils.split(subDirPath, "/");
        for (final String subDir : subDirs) {
            File file = new File(tempHome, subDir);
            if (!file.exists()) {
                String[] candidates = tempHome.list(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        if (name.equalsIgnoreCase(subDir)) {
                            return true;
                        }
                        return false;
                    }

                });
                if (candidates.length == 0) {
                    tempHome = null;
                    break;
                } else {
                    tempHome = new File(tempHome, candidates[0]);
                }
            } else {
                tempHome = file;
            }
        }
        return tempHome;
    }

    /**
     * 
     * @param fileNameToFind
     * @param directoryFile
     * @param ignoreCase
     * @return
     */
    private String searchViewFile(File directoryFile, final String fileNameToFind,
            final boolean ignoreCase) {
        String[] viewFiles = directoryFile.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String fileName) {
                String _notDirectoryViewName = fileNameToFind;
                String _fileName = fileName;
                if (ignoreCase) {
                    _fileName = fileName.toLowerCase();
                    _notDirectoryViewName = fileNameToFind.toLowerCase();
                }
                // 忽略大小写
                if (_fileName.startsWith(_notDirectoryViewName) && new File(dir, fileName).isFile()) {
                    if (fileName.length() == fileNameToFind.length()
                            && fileNameToFind.lastIndexOf('.') != -1) {
                        return true;
                    }
                    if (fileName.length() > fileNameToFind.length()
                            && fileName.charAt(fileNameToFind.length()) == '.') {
                        return true;
                    }
                }
                return false;
            }
        });
        Arrays.sort(viewFiles);
        return viewFiles.length == 0 ? null : viewFiles[0];
    }

    //-------------------------------------------

    protected ViewDispatcher getViewDispatcher(Invocation inv) {
        ViewDispatcher viewDispatcher = (ViewDispatcher) SpringUtils.getBean(inv
                .getApplicationContext(), viewDispatcherName);
        if (viewDispatcher == null) {
            viewDispatcher = registerViewDispatcher(inv.getApplicationContext());
        }
        return viewDispatcher;
    }

    /**
     * 注册一个 {@link ViewDispatcher}定义到上下文中，以被这个类的所有实例使用
     * 
     * @return
     */
    protected ViewDispatcher registerViewDispatcher(WebApplicationContext applicationContext) {
        // 并发下，重复注册虽然不会错误，但没有必要重复注册
        synchronized (applicationContext) {
            if (SpringUtils.getBean(applicationContext, viewDispatcherName) == null) {
                GenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(
                        ViewDispatcherImpl.class);
                ((BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory())
                        .registerBeanDefinition(viewDispatcherName, beanDefinition);
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("registered bean definition:"
                                    + ViewDispatcherImpl.class.getName());
                }
            }
            return (ViewDispatcher) SpringUtils.getBean(applicationContext, viewDispatcherName);
        }
    }
}

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

import java.beans.Statement;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import net.paoding.rose.jade.dataaccess.DataAccessFactory;
import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.dataaccess.DataAccessFactoryAdapter;
import net.paoding.rose.jade.rowmapper.DefaultRowMapperFactory;
import net.paoding.rose.jade.rowmapper.RowMapperFactory;
import net.paoding.rose.jade.statement.Interpreter;
import net.paoding.rose.jade.statement.InterpreterFactory;
import net.paoding.rose.jade.statement.StatementWrapperProvider;
import net.paoding.rose.jade.statement.cached.CacheProvider;
import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

/**
 * {@link JadeBeanFactoryPostProcessor}
 * 配置在发布包下的applicationContext-jade.xml中，Spring容器完成其内部的标准初始化工作后将调用本处理器，识别
 * 符合Jade规范的 DAO 接口并将之配置为Spring容器的Bean定义，加入到Spring容器中。
 * <p>
 * 
 * <h1>=开关属性的设置=</h1>
 * <p>
 * 
 * jade在发布jar包时，将在发布包下的 applicationContext-jade.xml
 * 文件配置本处理器（也就说本类一定会被Spring容器执行），为使jade能够适应不同的应用环境和业务需要，jade特
 * 提供了一些系统属性设定约定，使得您可灵活地控制本处理器的行为，甚至将本处理器视为一个空处理器：
 * <p>
 * 
 * <strong>jade.context.spring</strong><br>
 * 如果设置了一个非空属性值(非空时应该填写什么值Jade不做规定)，表示jade的spring初始化工作不由本类负责。<br>
 * 所以，如果您觉得jade默认去自动扫描DAO接口并注册到Spring容器的行为是您不愿接受，您可以设置一个非空值给该属性，
 * 从而叫停jade的这个行为。
 * <p>
 * 
 * <strong>jade.context.spring.com.yourcompany.dao.UserDAO</strong><br>
 * 合法的值：0表示忽略，1表示肯定；除0和1外的设置（包括空值）都是非法的。<br>
 * 在jade的spring初始化工作由本类负责的前提下，将该属性设置为0表示该DAO不由本类负责读取并放到Spring容器中（即忽略之）；
 * 设置为1则表示该DAO由本类负责读取并放到Spring容器中（即肯定之）。<br>
 * 如果没有该系统属性，jade则读取它的上一级属性：jade.context.spring.com.yourcompany.dao
 * 并以此类推，直至 jade.context.spring.*。这类属性在Jade统称为开关属性。
 * <p>
 * 
 * <strong>jade.context.spring.*</strong><br>
 * 这个属性是所有开关属性的根，即类似 jade.context.spring.com 和 jade.context.spring.cn
 * 之类的开关属性，它的父亲是 jade.context.spring.*, 而非 jade.context.spring<br>
 * 如果没有设置这个根属性，jade 将等价于其被设置为1。您可以将之设置为0，
 * 这样就表示只有那些明确设置了开关属性为1的package或接口的类才由本处理器负责读取并放到Spring容器中。
 * <p>
 * 
 * <h1>=DAO的发现=</h1>
 * <p>
 * 首先，本处理器会调用 {@link RoseScanner#getJarOrClassesFolderResources()}
 * 获取类路径下的classes目录以及那些设置了rose标帜的jar包地址。
 * 为了使jar包中的DAO能够被本处理器识别，其设置的rose标识中必须含有dao或DAO。
 * <p>
 * 然后，本处理器将从classes目录或jar包中识别那些符合jade规范的DAO接口：
 * <ul>
 * <li>
 * DAO接口的package必须含有dao目录，如：dao.UserDAO、myapp.dao.UserDAO、myapp.dao.blog
 * .BlogDAO</li>
 * <li>DAO接口必须以大写DAO结尾，如：UserDAO、BlogDAO</li>
 * <li>DAO接口上必须标注@DAO注解（Jade在实现上通过读取二进制文件来进行判断，而非Class.forName）</li>
 * </ul>
 * <p>
 * 通过这两个步骤，本处理器完成了对DAO接口的发现，并最后将这些接口封装为 {@link JadeFactoryBean}
 * 的形式注册到Spring容器中。
 * 
 * <h1>=数据源=</h1>
 * <p>
 * 数据源 {@link DataSource} 提供了数据库的访问接口，jade通过{@link DataSourceFactory}
 * 接口为DAO方法提供数据源，在本处利器所初始化的spring容器中，数据源的设置有两种方式：
 * 
 * <h2>==定制方式==</h2><br>
 * <ul>
 * <li>当spring容器配置了一个id/name 为 "jade.dataSourceFactory"
 * 对象，jade将把这个bean取出来，作为 {@link DataSourceFactory}为DAO提供数据源；</li>
 * <li>当spring容器没有id/name 为 "jade.dataSourceFactory"的对象，但是配置其它名字的
 * {@link DataSourceFactory}，jade将把这个bean 取出来，为DAO提供数据源；</li>
 * <li>当spring容器没有id/name 为 "jade.dataSourceFactory"的对象，但其中存在
 * {@link DataSourceFactory}的个数超过1个，此时系统初始化的时侯不会跑出异常，但一旦开始进行进行DAO操作时，将抛出
 * IllegalStateException 异常。（参见 {@link SpringDataSourceFactoryDelegate}）</li>
 * </ul>
 * 
 * <h2>==默认方式==</h2><br>
 * 当spring容器没有配置任何 {@link DataSourceFactory} 时，jade将启用默认方式为DAO配置数据源，即使用
 * {@link SpringDataSourceFactory}
 * 为DAO提供数据源，从spring容器中寻找对应的数据源。对于给定的一个DAO接口，如
 * com.mycompany.myapp.dao.UserDAO, 其规则如下：
 * <p>
 * <ul>
 * <li>如果存在id/name为jade.dataSource.com.mycompany.myapp.dao.
 * UserDAO的数据源，则使用它作为这个DAO的数据源，否则逐级询问配置，直到顶一级包名：jade.dataSource.com</li>
 * <li>如果以上仍未能确定UserDAO的数据源，且UserDAO接口上的<code>@DAO</code>
 * 的catalog属性非空（假设其值为myteam.myapp），则视myteam.myapp等同于package名，执行前一个步骤的问询</li>
 * <ul>
 * <li>即按此顺序问询Spring容器的配置：jade.dataSource.myteam.myapp.UserDAO，...，jade.
 * dataSource.myteam</li>
 * </ul>
 * <li>
 * 如果以上仍未能确定UserDAO的数据源，则判断是否存在id/name为jade.dataSource、dataSource的数据源</li>
 * <li>
 * 如果以上仍未能确定UserDAO的数据源，则最终就是没有数据源，运行时将会有异常抛出</li>
 * </ul> <br>
 * 
 * <h1>=SQL解析器=</h1>
 * <p>
 * 当DAO方法被调用，执行数据库访问前，jade总是会先调用相应的SQL解析器，解析/改写SQL、设置相应的参数或运行时状态。<br>
 * Jade使用 {@link InterpreterFactory} 为每个DAO方法配置对应的解析器。 本处理器使用的
 * {@link InterpreterFactory} 是 {@link SpringInterpreterFactory}。<br>
 * {@link SpringInterpreterFactory}将获取配置在Spring容器中的 {@link Interpreter}
 * ，按照标注在其上的{@link Order}排序，设置给各个DAO方法。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class JadeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    /**
     * 开关属性前缀常量
     */
    private static final String propertyPrefix = "jade.context.spring";;

    /**
     * 日志记录器
     */
    private final Log logger = LogFactory.getLog(JadeBeanFactoryPostProcessor.class);

    /**
     * 数据存取器工厂，通过它可以得到一个对数据库进行操作的实现
     * <p>
     * 
     * @see #getDataAccessFactory(ConfigurableListableBeanFactory)
     */
    private DataAccessFactory dataAccessFactory;

    /**
     * 行映射器工厂，通过它可以得到一个数据库表的行映射器实现，使得从数据库读取的记录可以映射为一个对象
     * <p>
     * 
     * @see #getRowMapperFactory()
     */
    private RowMapperFactory rowMapperFactory;

    /**
     * 解释器工厂，通过它可以或得一个DAO方法对应的解析器数组，这些解析器数组将解析每一次DAO操作，进行SQL解析或设置运行时状态
     * <p>
     * 
     * @see #getInterpreterFactory(ConfigurableListableBeanFactory)
     */
    private InterpreterFactory interpreterFactory;
    
    /**
     * StatmentWrapper提供者的bean名称，为“none”等价于null
     */
    private String statmentWrapperProviderName;

    /**
     * 缓存提供者的bean名称，为“none”等价于null
     */
    private String cacheProviderName;

    // ------------------------------

    public DataAccessFactory getDataAccessFactory(ConfigurableListableBeanFactory beanFactory) {
        if (this.dataAccessFactory == null) {
            dataAccessFactory = new DataAccessFactoryAdapter(//
                    new SpringDataSourceFactoryDelegate(beanFactory));
        }
        return dataAccessFactory;
    }

    public InterpreterFactory getInterpreterFactory(ConfigurableListableBeanFactory beanFactory) {
        if (interpreterFactory == null) {
            interpreterFactory = new SpringInterpreterFactory(beanFactory);
        }
        return interpreterFactory;
    }

    public RowMapperFactory getRowMapperFactory() {
        if (rowMapperFactory == null) {
            rowMapperFactory = new DefaultRowMapperFactory();
        }
        return rowMapperFactory;
    }

    public String getCacheProviderName(ConfigurableListableBeanFactory beanFactory) {
        if (cacheProviderName == null) {
            String[] names = beanFactory.getBeanNamesForType(CacheProvider.class);
            if (names.length == 0) {
                cacheProviderName = "none";
            } else if (names.length == 1) {
                cacheProviderName = names[0];
            } else {
                String topPriority = "jade.cacheProvider";
                if (ArrayUtils.contains(names, topPriority)) {
                    cacheProviderName = topPriority;
                } else {
                    throw new IllegalStateException(
                            "required not more than 1 CacheProvider, but found " + names.length);
                }
            }
        }
        return "none".equals(cacheProviderName) ? null : cacheProviderName;
    }

    public String getStatementWrapperProvider(ConfigurableListableBeanFactory beanFactory) {
        if (statmentWrapperProviderName == null) {
            String[] names = beanFactory.getBeanNamesForType(StatementWrapperProvider.class);
            if (names.length == 0) {
                statmentWrapperProviderName = "none";
            } else if (names.length == 1) {
                statmentWrapperProviderName = names[0];
            } else {
                String topPriority = "jade.statmentWrapperProvider";
                if (ArrayUtils.contains(names, topPriority)) {
                    statmentWrapperProviderName = topPriority;
                } else {
                    throw new IllegalStateException(
                            "required not more than 1 StatmentWrapperProvider, but found " + names.length);
                }
            }
        }
        return "none".equals(statmentWrapperProviderName) ? null : statmentWrapperProviderName;
    }

    // ------------------------------

    /**
     * 本方法将在Spring容器完成内部的标准初始化工作后被调用，在此识别 Jade DAO
     * 接口并将配置为Spring容器的Bean定义，加入到Spring容器中。
     * <p>
     * 
     * 因为本类将配置在发布包的 applicationContext-jade.xml 文件中，所以在rose环境中，本类一定会生效！
     * 为了适应不同的应用环境，这里提供了一些机制使有更灵活的控制，请参考类级别的JavaDoc说明。
     * <p>
     * 
     * @see BeanFactoryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory)
     */
    @Override
    public final void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        String springFlag = System.getProperty(propertyPrefix);

        // 对于配置了jade.context.spring 系统属性的，表示jade的spring初始化工作不由本类负责。
        if (springFlag != null && springFlag.length() > 0) {
            logger.info("found " + propertyPrefix + "=" + springFlag);
            return;
        }

        // 其它情况则按既定的规则执行
        doPostProcessBeanFactory(beanFactory);
    }

    private void doPostProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 记录开始
        if (logger.isInfoEnabled()) {
            logger.info("[jade] starting ...");
        }

        // 1、获取标注rose标志的资源(ResourceRef)，即classes目录、在/META-INF/rose.properties或/META-INF/MENIFEST.MF配置了rose属性的jar包
        final List<ResourceRef> resources = findRoseResources();

        // 2、从获取的资源(resources)中，把rose=*、rose=DAO、rose=dao的筛选出来，并以URL的形式返回
        List<String> urls = findJadeResources(resources);

        // 3、从每个URL中找出符合规范的DAO接口，并将之以JadeFactoryBean的形式注册到Spring容器中
        findJadeDAODefinitions(beanFactory, urls);

        // 记录结束
        if (logger.isInfoEnabled()) {
            logger.info("[jade] exits");
        }
    }

    /*
     * 找出含有rose标帜的目录或jar包
     */
    private List<ResourceRef> findRoseResources() {
        final List<ResourceRef> resources;
        try {
            resources = RoseScanner.getInstance().getJarOrClassesFolderResources();
        } catch (IOException e) {
            throw new ApplicationContextException(
                    "error on getJarResources/getClassesFolderResources", e);
        }
        return resources;
    }

    /*
     * 找出含有dao、DAO标识的url
     */
    private List<String> findJadeResources(final List<ResourceRef> resources) {
        List<String> urls = new LinkedList<String>();
        for (ResourceRef ref : resources) {
            if (ref.hasModifier("dao") || ref.hasModifier("DAO")) {
                try {
                    Resource resource = ref.getResource();
                    File resourceFile = resource.getFile();
                    if (resourceFile.isFile()) {
                        urls.add("jar:file:" + resourceFile.toURI().getPath()
                                + ResourceUtils.JAR_URL_SEPARATOR);
                    } else if (resourceFile.isDirectory()) {
                        urls.add(resourceFile.toURI().toString());
                    }
                } catch (IOException e) {
                    throw new ApplicationContextException("error on resource.getFile", e);
                }
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("[jade] found " + urls.size() + " jade urls: " + urls);
        }
        return urls;
    }

    /*
     * 从获得的目录或jar包中寻找出符合规范的DAO接口，并注册到Spring容器中
     */
    private void findJadeDAODefinitions(ConfigurableListableBeanFactory beanFactory,
            List<String> urls) {
        JadeComponentProvider provider = new JadeComponentProvider();
        Set<String> daoClassNames = new HashSet<String>();

        for (String url : urls) {
            if (logger.isInfoEnabled()) {
                logger.info("[jade] call 'jade/find'");
            }

            Set<BeanDefinition> dfs = provider.findCandidateComponents(url);
            if (logger.isInfoEnabled()) {
                logger.info("[jade] found " + dfs.size() + " beanDefinition from '" + url + "'");
            }

            for (BeanDefinition beanDefinition : dfs) {
                String daoClassName = beanDefinition.getBeanClassName();
                if (getDisableFlag(daoClassName)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[jade] ignored disabled jade dao class: " + daoClassName
                                + "  [" + url + "]");
                    }
                    continue;
                }
                if (daoClassNames.contains(daoClassName)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[jade] ignored replicated jade dao class: " + daoClassName
                                + "  [" + url + "]");
                    }
                    continue;
                }
                daoClassNames.add(daoClassName);

                registerDAODefinition(beanFactory, beanDefinition);
            }
        }
    }

    /*
     * 将找到的一个DAO接口注册到Spring容器中
     */
    private void registerDAODefinition(ConfigurableListableBeanFactory beanFactory,
            BeanDefinition beanDefinition) {
        final String daoClassName = beanDefinition.getBeanClassName();
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        /*
         * 属性及其设置要按 JadeFactoryBean 的要求来办
         */
        propertyValues.addPropertyValue("objectType", daoClassName);
        propertyValues.addPropertyValue("dataAccessFactory", getDataAccessFactory(beanFactory));
        propertyValues.addPropertyValue("rowMapperFactory", getRowMapperFactory());
        propertyValues.addPropertyValue("interpreterFactory", getInterpreterFactory(beanFactory));
        String cacheProviderName = getCacheProviderName(beanFactory);
        if (cacheProviderName != null) {
            RuntimeBeanReference beanRef = new RuntimeBeanReference(cacheProviderName);
            propertyValues.addPropertyValue("cacheProvider", beanRef);
        }
        String statementWrapperProvider = getStatementWrapperProvider(beanFactory);
        if (statementWrapperProvider != null) {
            RuntimeBeanReference beanRef = new RuntimeBeanReference(statementWrapperProvider);
            propertyValues.addPropertyValue("statementWrapperProvider", beanRef);
        }
        ScannedGenericBeanDefinition scannedBeanDefinition = (ScannedGenericBeanDefinition) beanDefinition;
        scannedBeanDefinition.setPropertyValues(propertyValues);
        scannedBeanDefinition.setBeanClass(JadeFactoryBean.class);

        DefaultListableBeanFactory defaultBeanFactory = (DefaultListableBeanFactory) beanFactory;
        defaultBeanFactory.registerBeanDefinition(daoClassName, beanDefinition);

        if (logger.isDebugEnabled()) {
            logger.debug("[jade] register DAO: " + daoClassName);
        }
    }

    /*
     * 获取给定dao类的开关属性
     */
    protected boolean getDisableFlag(String daoType) {
        String name = daoType;
        while (true) {
            String flag;
            if (name.length() == 0) {
                flag = System.getProperty(propertyPrefix + ".*");
            } else {
                flag = System.getProperty(propertyPrefix + "." + name);
            }
            if (flag == null) {
                int index = name.lastIndexOf('.');
                if (index == -1) {
                    if (name.length() == 0) {
                        return false;
                    } else {
                        name = "";
                    }
                } else {
                    name = name.substring(0, index);
                }
                continue;
            }
            if ("0".equals(flag)) {
                return true;
            } else if (flag == null || "1".equals(flag)) {
                return false;
            } else {
                if (name.length() == 0) {
                    name = "*";
                }
                throw new IllegalArgumentException("illegal value of property: " + propertyPrefix
                        + "." + name + "='" + flag + "'");
            }
        }
    }
}

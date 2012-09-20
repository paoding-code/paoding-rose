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
package net.paoding.rose.jade.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;


/**
 * 
 * 用此{@link DAO}注解标注在一个符合Jade编写规范的DAO接口类上，明确标注这是Jade DAO接口。
 * 
 * <p>
 * 一个Jade DAO需要符合以下基本要求：
 * <ul>
 * <li>1、 在dao package或子package下，如com.renren.myapp.dao；</li>
 * <li>2、 是一个public的java interface 类型；</li>
 * <li>3、 名称必须以大写DAO字母结尾，如UserDAO；</li>
 * <li>4、 必须标注@DAO 注解；</li>
 * <li>5、 不是其它类的内部接口；</li>
 * <p>
 * 
 * 如果DAO接口被打包成为一个jar的，为了要被Jade识别，必须在这个jar的 META-INFO/rose.properties
 * 文件中包含这个属性：rose=dao (rose=*亦可)。
 * 
 * 
 * @see http://code.google.com/p/paoding-rose/wiki/Jade_DAO_Spec
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DAO {

    /**
     * 指定所使用数据库连接的catalog属性，设置为空(空串)等价于没有设置，表示不使用catalog属性。
     * <p>
     * 
     * 一般情况下您不需要做任何设置，除非您所在的公司或组织有进一步的规范。
     * <p>
     * 
     * catalog的意义请参考 {@link Connection#setCatalog(String)}
     * ，特别地，在支持垂直切分的数据源中，也可以使用catalog作为切分的一个参考
     * <p>
     * 
     * @see Connection#setCatalog(String)
     * @return
     */
    String catalog() default "";
}

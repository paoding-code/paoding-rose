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
package net.paoding.rose.jade.dataaccess.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.dataaccess.DataSourceHolder;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.springframework.util.CollectionUtils;

/**
 * 在master-slave模式下的应用程序可以使用，即所有写操作使用master，所有读操作使用slave
 * <p>
 * 
 * 以下演示了使用他的Java代码（Spring配置文件的配置也可以参考以下代码并进行等价转化)
 * 
 * <pre>
 * MasterSlaveDataSourceFactory mainFactory = new MasterSlaveDataSourceFactory();
 * 
 * DataSource master = getMasterDataSource();
 * mainFactory.setMasters(new SimpleDataSourceFactory(master));
 * 
 * List&lt;DataSource&gt; slaves = getSlaveDataSources();
 * if (queryFromMaster) {
 *     slaves = new ArrayList&lt;DataSource&gt;(slaves);
 *     slaves.add(master);
 * }
 * mainFactory.setSlaves(new RandomDataSourceFactory(slaves));
 * </pre>
 * 
 * @author qieqie
 * 
 */
public class MasterSlaveDataSourceFactory implements DataSourceFactory {

    private DataSourceFactory masters = new RandomDataSourceFactory();

    private DataSourceFactory slaves = new RandomDataSourceFactory();

    public MasterSlaveDataSourceFactory() {
    }

    /**
     * 
     * @param master
     * @param slaves
     * @param queryFromMaster true代表允许从master数据源查询数据
     */
    public MasterSlaveDataSourceFactory(DataSource master, List<DataSource> slaves,
            boolean queryFromMaster) {
        if (queryFromMaster && !CollectionUtils.containsInstance(slaves, master)) {
            slaves = new ArrayList<DataSource>(slaves);
            slaves.add(master);
        }
        setSlaves(new RandomDataSourceFactory(slaves));
        setMasters(new SimpleDataSourceFactory(master));
    }

    //------------------

    /**
     * 
     * @param masters
     * @see RandomDataSourceFactory
     * @see SimpleDataSourceFactory
     */
    public void setMasters(DataSourceFactory masters) {
        this.masters = masters;
    }

    /**
     * 
     * @param slaves
     * @see RandomDataSourceFactory
     */
    public void setSlaves(DataSourceFactory slaves) {
        this.slaves = slaves;
    }

    @Override
    public DataSourceHolder getHolder(StatementMetaData metaData,
            Map<String, Object> runtimeProperties) {
        if (metaData.getSQLType() != SQLType.READ) {
            return masters.getHolder(metaData, runtimeProperties);
        } else {
            return slaves.getHolder(metaData, runtimeProperties);
        }
    }
}

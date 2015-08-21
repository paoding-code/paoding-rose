/*
 * Copyright 2007-2012 the original author or authors.
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
package net.paoding.rose.web.portal.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 此类用于配置在spring文件中，使获取Spring的 {@link ThreadPoolTaskExecutor} 的内部 {@link ExecutorService}对象
 * @author  王志亮 [qieqie.wang@gmail.com]
 *
 */
public class ThreadPoolExcutorServiceFactoryBean implements FactoryBean {

	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}
	
	@Override
	public Object getObject() throws Exception {
		return threadPoolTaskExecutor.getThreadPoolExecutor();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return ThreadPoolExecutor.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}

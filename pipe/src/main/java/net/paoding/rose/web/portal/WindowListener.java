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
package net.paoding.rose.web.portal;

/**
 * 窗口的状态侦听器
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface WindowListener {

    /**
     * 当添加一个窗口到aggregate时被调用
     * 
     * @param window
     */
    public void onWindowAdded(Window window);

    /**
     * 当窗口开始被容器处理时被调用
     * 
     * @param window
     */
    public void onWindowStarted(Window window);

    /**
     * 当窗口被取消时候调用
     * 
     * @param window
     */
    public void onWindowCanceled(Window window);

    /**
     * 当窗口被成功执行后被调用(即：没有抛出异常时候)
     * 
     * @param window
     */
    public void onWindowDone(Window window);

    /**
     * 当窗口执行出现异常时被调用(异常对象可通过window.getThrowable()方法获取)
     * 
     * @param window
     */
    public void onWindowError(Window window);

    /**
     * 当aggregate等待窗口超时时被调用
     * 
     * @param window
     */
    public void onWindowTimeout(Window window);

}

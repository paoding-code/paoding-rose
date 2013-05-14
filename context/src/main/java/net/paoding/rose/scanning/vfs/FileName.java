/*
 * Copyright 2007-2010 the original author or authors.
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
package net.paoding.rose.scanning.vfs;

import java.io.IOException;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface FileName {

    /**
     * 返回所代表的文件或实体对象
     * 
     * @return
     * @throws IOException
     */
    public FileObject getFileObject() throws IOException;

    /**
     * 基础文件名,即使是目录也不以'/'开始或结尾
     * 
     * @return
     * @throws IOException
     */
    public String getBaseName() throws IOException;

    /**
     * 一个下级文件或实体相对于本文件或实体的路径，得到的返回字符串不以'/'开始，如果subFileName是一个子目录的话，返回的结果将以
     * '/'结尾
     * 
     * @param subFileName
     * @return
     * @throws IOException
     */
    public String getRelativeName(FileName subFileName) throws IOException;

}

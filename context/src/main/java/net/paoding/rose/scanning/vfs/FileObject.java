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
import java.net.URL;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface FileObject {

    /**
     * 该文件是否存在
     * 
     * @return
     * @throws IOException
     */
    boolean exists() throws IOException;

    /**
     * 返回该文件或实体的名称对象
     * 
     * @return
     * @throws IOException
     */
    FileName getName() throws IOException;

    /**
     * 返回子文件或实体(包括目录)
     * 
     * @return
     * @throws IOException
     */
    FileObject[] getChildren() throws IOException;

    /**
     * 文件的类型，目录或文件。jar文件里面实体也可能是目录或文件。
     * 
     * @return
     * @throws IOException
     */
    FileType getType() throws IOException;

    /**
     * 返回该文件或实体的URL对象
     * 
     * @return
     * @throws IOException
     */
    URL getURL() throws IOException;

    /**
     * 父目录对象
     * 
     * @return
     * @throws IOException
     */
    FileObject getParent() throws IOException;

    /**
     * 子文件或实体
     * 
     * @param name
     * @return
     * @throws IOException
     */
    FileObject getChild(String name) throws IOException;

    /**
     * 文件实体内容，如果是目录将抛出IOE异常
     * 
     * @return
     * @throws IOException
     */
    FileContent getContent() throws IOException;

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();

}

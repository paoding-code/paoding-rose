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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.util.ResourceUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JarFileObject implements FileObject {

    // 文件解析管理器，可保证同样的url对应同一个fileObject对象
    private final FileSystemManager fs;

    // 该文件对象的url，如jar:file:/path/to/your/jarfile.jar!/net/paoding/
    private final URL url;

    // 该文件对象的url，如jar:file:/path/to/your/jarfile.jar!/net/paoding/
    private final String urlString;

    // 文件名称对象，用来获取文件名称、计算相对地址等
    private final FileName fileName;

    // 该文件所属的jar文件的根级FileObject对象，如jar:file:/path/to/your/jarfile.jar!/
    private final JarFileObject root;

    // 该文件所属的jar文件的JarFile对象
    private final JarFile jarFile;

    // 该文件在JarFile中的JarEntry对象，如net/paoding， com/yourcampany/yourapp
    // 如果是根地址，如xxx.jar!/，entry将为null
    private final JarEntry entry;

    JarFileObject(FileSystemManager fs, URL url) throws FileNotFoundException, IOException {
        this.fs = fs;
        String urlString = url.toString();
        String entryName = urlString.substring(urlString.indexOf(ResourceUtils.JAR_URL_SEPARATOR)
                + ResourceUtils.JAR_URL_SEPARATOR.length());
        if (entryName.length() == 0) {
            this.root = this;
            int beginIndex = urlString.indexOf(ResourceUtils.FILE_URL_PREFIX)
                    + ResourceUtils.FILE_URL_PREFIX.length();
            int endIndex = urlString.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
            this.jarFile = new JarFile(urlString.substring(beginIndex, endIndex));
        } else {
            this.root = (JarFileObject) fs.resolveFile(urlString.substring(//
                    0, urlString.indexOf(ResourceUtils.JAR_URL_SEPARATOR)
                            + ResourceUtils.JAR_URL_SEPARATOR.length()));
            this.jarFile = root.jarFile;
        }
        this.entry = jarFile.getJarEntry(entryName);
        this.url = url;
        this.urlString = urlString;
        int indexSep = entryName.lastIndexOf('/');
        if (indexSep == -1) {
            this.fileName = new FileNameImpl(this, entryName);
        } else {
            if (entryName.endsWith("/")) {
                int index = entryName.lastIndexOf('/', entryName.length() - 2);
                this.fileName = new FileNameImpl(this, entryName.substring(index + 1, indexSep));
            } else {
                this.fileName = new FileNameImpl(this, entryName.substring(indexSep + 1));
            }
        }
    }

    @Override
    public FileObject getChild(String name) throws IOException {
        return fs.resolveFile(urlString + name);
    }

    @Override
    public FileObject[] getChildren() throws IOException {
        List<FileObject> children = new LinkedList<FileObject>();
        Enumeration<JarEntry> e = jarFile.entries();
        String entryName = root == this ? "" : entry.getName();
        while (e.hasMoreElements()) {
            JarEntry entry = e.nextElement();
            if (entry.getName().length() > entryName.length()
                    && entry.getName().startsWith(entryName)) {
                int index = entry.getName().indexOf('/', entryName.length() + 1);
                // 儿子=文件或子目录
                if (index == -1 || index == entry.getName().length() - 1) {
                    children.add(fs.resolveFile(root.urlString + entry.getName()));
                }
            }
        }
        return children.toArray(new FileObject[0]);
    }

    @Override
    public FileContent getContent() throws IOException {
        if (getType() != FileType.FILE) {
            throw new IOException("can not read");
        }
        return new FileContent() {

            @Override
            public InputStream getInputStream() throws IOException {
                return getURL().openStream();
            }
        };
    }

    @Override
    public FileName getName() throws IOException {
        return fileName;
    }

    @Override
    public FileObject getParent() throws IOException {
        if (entry == null) {
            return null;
        }
        if (entry.getName().length() == 0) {
            return null;
        }
        int lastSep = entry.getName().lastIndexOf('/');
        if (lastSep == -1) {
            return root;
        }
        String entryName = entry.getName();
        String parentEntryName;
        if (entry.isDirectory()) {
            parentEntryName = entryName.substring(0, 1 + entryName.lastIndexOf('/', entryName
                    .length() - 2));
        } else {
            parentEntryName = entryName.substring(0, 1 + lastSep);
        }
        return fs.resolveFile(root.urlString + parentEntryName);
    }

    @Override
    public FileType getType() throws IOException {
        return (entry == null || entry.isDirectory()) ? FileType.FOLDER : FileType.FILE;
    }

    @Override
    public URL getURL() throws IOException {
        return url;
    }

    @Override
    public boolean exists() throws IOException {
        return root == this || entry != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JarFileObject)) {
            return false;
        }
        JarFileObject t = (JarFileObject) obj;
        return this.urlString.equals(t.urlString);
    }

    @Override
    public int hashCode() {
        return urlString.hashCode() * 13;
    }

    @Override
    public String toString() {
        return urlString;
    }

}

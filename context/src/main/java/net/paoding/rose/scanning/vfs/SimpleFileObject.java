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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.util.ResourceUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class SimpleFileObject implements FileObject {

    private final URL url;

    private final String urlString;

    private final File file;

    private final FileName fileName;

    private final FileSystemManager fs;

    SimpleFileObject(FileSystemManager fs, URL url) throws FileNotFoundException,
            MalformedURLException {
        this.fs = fs;
        File file = ResourceUtils.getFile(url);
        String urlString = url.toString();
        this.url = url;
        this.file = file;
        this.urlString = urlString;
        this.fileName = new FileNameImpl(this, file.getName());
    }

    @Override
    public FileObject getChild(final String child) throws IOException {
        return fs.resolveFile(urlString + child);
    }

    @Override
    public FileObject[] getChildren() throws MalformedURLException, IOException {
        File[] files = file.listFiles();
        FileObject[] children = new FileObject[files.length];
        for (int i = 0; i < children.length; i++) {
            if (files[i].isDirectory()) {
                children[i] = fs.resolveFile(urlString + files[i].getName() + "/");
            } else {
                children[i] = fs.resolveFile(urlString + files[i].getName());
            }
        }
        return children;
    }

    @Override
    public FileContent getContent() throws IOException {
        if (!file.canRead()) {
            throw new IOException("can not read");
        }
        return new FileContent() {

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }
        };
    }

    @Override
    public FileName getName() {
        return fileName;
    }

    @Override
    public FileObject getParent() throws MalformedURLException, IOException {
        File parent = file.getParentFile();
        if (parent == null) {
            return null;
        }
        return fs.resolveFile(parent.toURI().toURL());
    }

    @Override
    public FileType getType() {
        if (file.isFile()) {
            return FileType.FILE;
        } else if (file.isDirectory()) {
            return FileType.FOLDER;
        }
        return FileType.UNKNOWN;
    }

    @Override
    public URL getURL() throws MalformedURLException {
        return url;
    }

    @Override
    public boolean exists() throws IOException {
        return file.exists();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleFileObject)) {
            return false;
        }
        SimpleFileObject t = (SimpleFileObject) obj;
        return this.file.equals(t.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode() * 13;
    }

    @Override
    public String toString() {
        return urlString;
    }

}

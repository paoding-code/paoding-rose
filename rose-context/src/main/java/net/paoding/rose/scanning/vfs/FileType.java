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

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class FileType {

    public static final FileType FOLDER = new FileType(true, false);

    public static final FileType FILE = new FileType(false, true);

    public static final FileType UNKNOWN = new FileType(false, false);

    private boolean hasChildren = false;

    private boolean hasContent = false;

    private FileType(boolean hasChildren, boolean hasContent) {
        this.hasChildren = hasChildren;
        this.hasContent = hasContent;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public boolean hasContent() {
        return hasContent;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileType)) {
            return false;
        }
        FileType t = (FileType) obj;
        return hasChildren == t.hasChildren && hasContent == t.hasContent;
    }

    @Override
    public String toString() {
        return hasChildren ? "FOLDER" : hasContent ? "FILE" : "UNKNOWN";
    }

}

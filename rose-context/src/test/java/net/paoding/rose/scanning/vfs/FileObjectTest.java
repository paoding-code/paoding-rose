package net.paoding.rose.scanning.vfs;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.ResourceUtils;

public class FileObjectTest extends TestCase {

    private static ClassLoader loader = SimpleFileObject.class.getClassLoader();

    public void testSimpleFileObjectDirEnd() throws IOException {
        FileSystemManager fs = new FileSystemManager();
        String pathDir = "net/paoding/rose";
        URL urlDir = loader.getResource(pathDir);
        FileObject parent = fs.resolveFile(urlDir);
        assertTrue(parent.getURL().toString().endsWith("/"));
        assertTrue(parent.getURL().getPath().endsWith("/"));
        FileObject childDir = parent.getChild("scanning");
        assertTrue(childDir.getURL().toString().endsWith("/"));

        // getChildren
        assertTrue(ArrayUtils.contains(parent.getChildren(), childDir));
    }

    public void testSimpleFileObject() throws IOException {

        FileSystemManager fs = new FileSystemManager();
        //
        String pathFile = "net/paoding/rose/scanning/vfs/SimpleFileObject.class";
        URL urlFile = loader.getResource(pathFile);
        assertNotNull(urlFile);
        FileObject fileObjectFile = fs.resolveFile(urlFile);
        assertEquals(SimpleFileObject.class, fileObjectFile.getClass());
        //
        String pathDir = new File(urlFile.getPath()).getParent().replace('\\', '/');
        pathDir = StringUtils.removeEnd(pathDir, "/");
        URL urlDir = ResourceUtils.getURL(pathDir);
        assertNotNull(urlDir);
        File fileDir = new File(urlDir.getFile());
        assertTrue(fileDir.exists());
        FileObject fileObjectDir = fs.resolveFile(urlDir);
        assertEquals(SimpleFileObject.class, fileObjectDir.getClass());

        File dirFile = ResourceUtils.getFile(urlDir);
        assertTrue(urlDir.toString().endsWith("/"));
        assertTrue(urlDir.getPath().endsWith("/"));
        assertFalse(dirFile.getPath().endsWith("/"));
        assertTrue(fileObjectDir.toString().endsWith("/"));

        // exists
        assertTrue(fileObjectFile.exists());
        assertTrue(fileObjectDir.exists());
        assertFalse(fileObjectDir.getChild("a_not_exists_file.txt").exists());

        // getName
        assertEquals("vfs", fileObjectDir.getName().getBaseName());
        assertEquals("SimpleFileObject.class", fileObjectFile.getName().getBaseName());

        // getRelativeName
        assertEquals("SimpleFileObject.class", fileObjectDir.getName().getRelativeName(
                fileObjectFile.getName()));
        assertEquals("", fileObjectDir.getName().getRelativeName(fileObjectDir.getName()));
        assertEquals("", fileObjectFile.getName().getRelativeName(fileObjectFile.getName()));

        // getType
        assertSame(FileType.FOLDER, fileObjectDir.getType());
        assertSame(FileType.FILE, fileObjectFile.getType());

        // getChild, getParent, and equals, getChildren
        assertEquals(fileObjectFile, fileObjectDir.getChild("SimpleFileObject.class"));
        assertEquals(fileObjectDir, fileObjectFile.getParent());
        assertSame(fileObjectFile, fileObjectDir.getChild("SimpleFileObject.class"));
        assertSame(fileObjectDir, fileObjectFile.getParent());
        assertTrue(ArrayUtils.contains(fileObjectDir.getChildren(), fileObjectFile));

        // getURL
        assertEquals(urlDir, fileObjectDir.getURL());
        assertEquals(urlFile, fileObjectFile.getURL());
    }

    public void testJarFileObjectDirEnd() throws IOException {
        FileSystemManager fs = new FileSystemManager();
        String pathDir = "org/apache/commons/lang/";
        URL urlDir = loader.getResource(pathDir);
        FileObject parent = fs.resolveFile(urlDir);
        assertTrue(parent.exists());
        assertTrue(parent.getURL().toString().endsWith("/"));
        FileObject childDir = parent.getChild("math");
        FileObject childDir2 = parent.getChild("math/");
        assertTrue(childDir.exists());
        assertTrue(childDir.getURL().toString().endsWith("/"));
        assertSame(childDir, childDir2);

        assertEquals("math/", parent.getName().getRelativeName(childDir.getName()));
        assertEquals("math/", parent.getName().getRelativeName(childDir2.getName()));
    }

    public void testJarFileObject() throws IOException {

        FileSystemManager fs = new FileSystemManager();

        URL urlFile = loader.getResource("org/apache/commons/lang/StringUtils.class");
        assertTrue(urlFile.toString().startsWith("jar:"));
        FileObject fileObjectFile = fs.resolveFile(urlFile);
        assertEquals(JarFileObject.class, fileObjectFile.getClass());
        //

        URL urlDir = new URL(urlFile.toString().substring(0,
                urlFile.toString().lastIndexOf('/') + 1));
        assertNotNull(urlDir);
        FileObject fileObjectDir = fs.resolveFile(urlDir);
        assertEquals(JarFileObject.class, fileObjectDir.getClass());

        // exists
        assertTrue(fileObjectFile.exists());
        assertTrue(fileObjectDir.exists());
        assertFalse(fileObjectDir.getChild("a_not_exists_file.txt").exists());

        // getName
        assertEquals("lang", fileObjectDir.getName().getBaseName());
        assertEquals("StringUtils.class", fileObjectFile.getName().getBaseName());

        // getRelativeName
        assertEquals("StringUtils.class", fileObjectDir.getName().getRelativeName(
                fileObjectFile.getName()));
        assertEquals("", fileObjectDir.getName().getRelativeName(fileObjectDir.getName()));
        assertEquals("", fileObjectFile.getName().getRelativeName(fileObjectFile.getName()));

        // getType
        assertSame(FileType.FOLDER, fileObjectDir.getType());
        assertSame(FileType.FILE, fileObjectFile.getType());

        // getChild, getParent, and equals
        assertEquals(fileObjectFile, fileObjectDir.getChild("StringUtils.class"));
        assertEquals(fileObjectDir, fileObjectFile.getParent());
        assertSame(fileObjectFile, fileObjectDir.getChild("StringUtils.class"));
        assertSame(fileObjectDir, fileObjectFile.getParent());
        assertTrue(ArrayUtils.contains(fileObjectDir.getChildren(), fileObjectFile));

        // getURL
        assertEquals(urlDir, fileObjectDir.getURL());
        assertEquals(urlFile, fileObjectFile.getURL());

    }

    public void testJarFileRootObject() throws IOException {

        FileSystemManager fs = new FileSystemManager();

        URL urlFile = loader.getResource("org/apache/commons/lang/StringUtils.class");
        String urlString = urlFile.toString();
        int index = urlString.indexOf("!/");
        assertTrue(index > 0);
        String root = urlString.substring(0, index + 2);
        FileObject rootObject = fs.resolveFile(root);
        assertTrue(root, rootObject.exists());

        assertTrue(rootObject.getChildren().length > 0);
        assertTrue(rootObject.getChild("org/").exists());
        assertTrue(rootObject.getChild("org/") == fs.resolveFile(root + "org/"));
    }
}

package net.paoding.rose.scanning.vfs;

import org.apache.commons.lang.ArrayUtils;

import junit.framework.TestCase;
import net.paoding.rose.scanning.LoadScope;

public class LoadScopeTest extends TestCase {

    public void test1() {
        LoadScope scope = new LoadScope("controllers=com.xiaonei.game", "controllers");
        assertNull(scope.getScope("applicationContext"));
        assertTrue(ArrayUtils.isEquals(new String[] { "com.xiaonei.game", "net.paoding.rose" },
                scope.getScope("controllers")));
    }

    public void test2() {
        LoadScope scope = new LoadScope("controllers=com.xiaonei.game,net.paoding.rose.web",
                "controllers");
        assertNull(scope.getScope("applicationContext"));
        assertTrue(ArrayUtils.isEquals(new String[] { "com.xiaonei.game", "net.paoding.rose.web",
                "net.paoding.rose" }, scope.getScope("controllers")));
    }

    public void test3() {
        LoadScope scope = new LoadScope(
                "com.xiaonei.game;applicationContext=com.xiaonei.abc", "controllers");
        assertNotNull(scope.getScope("applicationContext"));
        assertTrue(ArrayUtils.isEquals(new String[] { "com.xiaonei.game", "net.paoding.rose" },
                scope.getScope("controllers")));
        assertTrue(ArrayUtils.isEquals(new String[] { "com.xiaonei.abc", "net.paoding.rose" },
                scope.getScope("applicationContext")));
    }
}

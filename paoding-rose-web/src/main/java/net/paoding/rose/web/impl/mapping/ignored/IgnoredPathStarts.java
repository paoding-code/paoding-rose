package net.paoding.rose.web.impl.mapping.ignored;

import net.paoding.rose.web.RequestPath;

public class IgnoredPathStarts implements IgnoredPath {

    private String path;

    public IgnoredPathStarts(String path) {
        this.path = path;
    }

    @Override
    public boolean hit(RequestPath requestPath) {
        return requestPath.getRosePath().startsWith(path);
    }
}
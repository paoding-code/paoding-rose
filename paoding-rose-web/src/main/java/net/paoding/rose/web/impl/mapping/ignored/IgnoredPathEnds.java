package net.paoding.rose.web.impl.mapping.ignored;

import net.paoding.rose.web.RequestPath;

public class IgnoredPathEnds implements IgnoredPath {

    private String path;

    public IgnoredPathEnds(String path) {
        this.path = path;
    }

    @Override
    public boolean hit(RequestPath requestPath) {
        return requestPath.getRosePath().endsWith(path);
    }
}

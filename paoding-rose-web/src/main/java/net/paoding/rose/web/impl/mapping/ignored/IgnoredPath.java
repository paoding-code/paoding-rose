package net.paoding.rose.web.impl.mapping.ignored;

import net.paoding.rose.web.RequestPath;

public interface IgnoredPath {

    public boolean hit(RequestPath requestPath);
}

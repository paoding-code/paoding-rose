/*
 * Copyright 2007-2009 the original author or authors.
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
package net.paoding.rose.web.portal.impl;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalResolver implements ParamResolver {

    private PortalFactory portalFactory;

    public void setPortalFactory(PortalFactory portalFactory) {
        this.portalFactory = portalFactory;
    }

    public PortalFactory getPortalFactory() {
        return portalFactory;
    }

    @Override
    public boolean supports(ParamMetaData paramMetaData) {
        if (portalFactory == null) {
            return false;
        }
        return paramMetaData.getParamType() == Portal.class;
    }

    @Override
    public Portal resolve(Invocation inv, ParamMetaData paramMetaData) throws Exception {
        return portalFactory.createPortal(inv);
    }

}

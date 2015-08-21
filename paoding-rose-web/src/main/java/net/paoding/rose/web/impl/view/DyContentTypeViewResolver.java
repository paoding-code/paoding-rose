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
package net.paoding.rose.web.impl.view;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class DyContentTypeViewResolver implements ViewResolver {

    private ViewResolver viewResolver;

    private String contentType;

    public DyContentTypeViewResolver(ViewResolver viewResolver, String contentType) {
        this.viewResolver = viewResolver;
        this.contentType = contentType;
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        View view = viewResolver.resolveViewName(viewName, locale);
        if (contentType != null && view instanceof AbstractView) {
            ((AbstractView) view).setContentType(contentType);
        }
        return view;
    }

}

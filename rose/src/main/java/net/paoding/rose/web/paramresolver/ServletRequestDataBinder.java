/*
 * Copyright 2002-2011 the original author or authors.
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
package net.paoding.rose.web.paramresolver;

import java.util.Date;

import javax.servlet.ServletRequest;

import net.paoding.rose.web.paramresolver.ResolverFactoryImpl.DateEditor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyValue;
import org.springframework.validation.AbstractPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

/**
 * Special {@link org.springframework.validation.DataBinder} to perform
 * data binding from servlet request parameters to JavaBeans, including
 * support for multipart files.
 * 
 * <p>
 * See the DataBinder/WebDataBinder superclasses for customization options,
 * which include specifying allowed/required fields, and registering custom
 * property editors.
 * 
 * <p>
 * Used by Spring Web MVC's BaseCommandController and
 * MultiActionController. Note that BaseCommandController and its
 * subclasses allow for easy customization of the binder instances that
 * they use through overriding <code>initBinder</code>.
 * 
 * <p>
 * Can also be used for manual data binding in custom web controllers: for
 * example, in a plain Controller implementation or in a
 * MultiActionController handler method. Simply instantiate a
 * ServletRequestDataBinder for each binding process, and invoke
 * <code>bind</code> with the current ServletRequest as argument:
 * 
 * <pre class="code"> MyBean myBean = new MyBean(); // apply binder to
 * custom target object ServletRequestDataBinder binder = new
 * ServletRequestDataBinder(myBean); // register custom editors, if desired
 * binder.registerCustomEditor(...); // trigger actual binding of request
 * parameters binder.bind(request); // optionally evaluate binding errors
 * Errors errors = binder.getErrors(); ...</pre>
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #bind(javax.servlet.ServletRequest)
 * @see #registerCustomEditor
 * @see #setAllowedFields
 * @see #setRequiredFields
 * @see #setFieldMarkerPrefix
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 */
public class ServletRequestDataBinder extends WebDataBinder {

    private String prefix;

    /**
     * Create a new ServletRequestDataBinder instance, with default object
     * name.
     * 
     * @param target the target object to bind onto (or <code>null</code>
     *        if the binder is just used to convert a plain parameter
     *        value)
     * @see #DEFAULT_OBJECT_NAME
     */
    public ServletRequestDataBinder(Object target) {
        super(target);
    }

    /**
     * Create a new ServletRequestDataBinder instance.
     * 
     * @param target the target object to bind onto (or <code>null</code>
     *        if the binder is just used to convert a plain parameter
     *        value)
     * @param objectName the name of the target object
     */
    public ServletRequestDataBinder(Object target, String objectName) {
        super(target, objectName);
        prefix = objectName + '.';
    }

    /**
     * Return the internal BindingResult held by this DataBinder, as
     * AbstractPropertyBindingResult.
     */
    @Override
    protected AbstractPropertyBindingResult getInternalBindingResult() {
        AbstractPropertyBindingResult bindingResult = super.getInternalBindingResult();

        // by rose
        PropertyEditorRegistry registry = bindingResult.getPropertyEditorRegistry();
        registry.registerCustomEditor(Date.class, new DateEditor(Date.class));
        registry.registerCustomEditor(java.sql.Date.class, new DateEditor(java.sql.Date.class));
        registry.registerCustomEditor(java.sql.Time.class, new DateEditor(java.sql.Time.class));
        registry.registerCustomEditor(java.sql.Timestamp.class, new DateEditor(
                java.sql.Timestamp.class));
        return bindingResult;
    }

    /**
     * Bind the parameters of the given request to this binder's target,
     * also binding multipart files in case of a multipart request.
     * <p>
     * This call can create field errors, representing basic binding errors
     * like a required field (code "required"), or type mismatch between
     * value and bean property (code "typeMismatch").
     * <p>
     * Multipart files are bound via their parameter name, just like normal
     * HTTP parameters: i.e. "uploadedFile" to an "uploadedFile" bean
     * property, invoking a "setUploadedFile" setter method.
     * <p>
     * The type of the target property for a multipart file can be
     * MultipartFile, byte[], or String. The latter two receive the
     * contents of the uploaded file; all metadata like original file name,
     * content type, etc are lost in those cases.
     * 
     * @param request request with parameters to bind (can be multipart)
     * @see org.springframework.web.multipart.MultipartHttpServletRequest
     * @see org.springframework.web.multipart.MultipartFile
     * @see #bindMultipartFiles
     * @see #bind(org.springframework.beans.PropertyValues)
     */
    public void bind(ServletRequest request) {
        MutablePropertyValues mpvs = new MutablePropertyValues(WebUtils.getParametersStartingWith(
                request, prefix));
        MultipartRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartRequest.class);
        if (multipartRequest != null) {
            bindMultipart(multipartRequest.getMultiFileMap(), mpvs);
        }
        addBindValues(mpvs, request);
        doBind(mpvs);
    }
    
    /**
     * Extension point that subclasses can use to add extra bind values for a
     * request. Invoked before {@link #doBind(MutablePropertyValues)}.
     * The default implementation is empty. 
     * @param mpvs the property values that will be used for data binding
     * @param request the current request
     */
    protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
    }

    @Override
    protected void doBind(MutablePropertyValues mpvs) {
        // book.author.name的设置要自动使book.author设置一个author进去
        PropertyValue[] pvArray = mpvs.getPropertyValues();
        MutablePropertyValues newMpvs = null;
        for (int i = 0; i < pvArray.length; i++) {
            PropertyValue pv = pvArray[i];
            String propertyName = pv.getName();
            int dot = propertyName.indexOf('.');
            while (dot != -1) {
                String field = propertyName.substring(0, dot);
                if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
                    Class<?> fieldType = getPropertyAccessor().getPropertyType(field);
                    if (newMpvs == null) {
                        newMpvs = new MutablePropertyValues();
                    }
                    newMpvs.addPropertyValue(field, BeanUtils.instantiateClass(fieldType));
                }
                dot = propertyName.indexOf('.', dot + 1);
            }
        }
        if (newMpvs == null) {
            super.doBind(mpvs);
        } else {
            newMpvs.addPropertyValues(mpvs);
            super.doBind(newMpvs);
        }
    }

    /**
     * Treats errors as fatal.
     * <p>
     * Use this method only if it's an error if the input isn't valid. This
     * might be appropriate if all input is from dropdowns, for example.
     * 
     * @throws ServletRequestBindingException subclass of ServletException
     *         on any binding problem
     */
    public void closeNoCatch() throws ServletRequestBindingException {
        if (getBindingResult().hasErrors()) {
            throw new ServletRequestBindingException("Errors binding onto object '"
                    + getBindingResult().getObjectName() + "'", new BindException(
                    getBindingResult()));
        }
    }

}

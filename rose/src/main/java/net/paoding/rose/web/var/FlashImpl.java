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
package net.paoding.rose.web.var;

import net.paoding.rose.util.Base64;
import net.paoding.rose.util.PlaceHolderUtils;
import net.paoding.rose.web.Invocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class FlashImpl implements Flash {

    private static final String DELIM = "___";

    private static final String cookiePrefix = DELIM + "flashMessage" + DELIM;

    private static Log logger = LogFactory.getLog(FlashImpl.class);

    private Map<String, String> last = Collections.emptyMap();

    private boolean lastRead = false;

    private Map<String, String> next = Collections.emptyMap();

    private static final Base64 base64 = new Base64();

    private Invocation invocation;

    public FlashImpl(Invocation invocation) {
        this.invocation = invocation;
    }

    protected synchronized void readLastMessages() {
        if (lastRead) {
            return;
        }
        lastRead = true;
        if (logger.isDebugEnabled()) {
            logger.debug("readLastMessages");
        }
        Cookie[] cookies = invocation.getRequest().getCookies();
        for (int i = 0; cookies != null && i < cookies.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("cookie " + cookies[i].getName() + "=" + cookies[i].getValue()
                        + "; age=" + cookies[i].getMaxAge());
            }
            if (cookies[i].getValue() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ignore cookie: " + cookies[i].getName());
                }
                continue;
            }
            if (cookies[i].getName().startsWith(cookiePrefix)) {
                StringTokenizer st = new StringTokenizer(cookies[i].getName(), DELIM);
                String[] splits = new String[st.countTokens()];
                for (int j = 0; j < splits.length; j++) {
                    splits[j] = st.nextToken();
                }
                if (splits.length < 2) {
                    if (logger.isInfoEnabled()) {
                        logger.info("ignore flash cookie: " + cookies[i].getName());
                    }
                    continue;
                }
                String name = splits[1];
                String cookieValue = cookies[i].getValue();
                String flashMessage;
                if (cookieValue.length() == 0) {
                    flashMessage = "";
                } else {
                    try {
                        flashMessage = new String(base64.decodeFromString(cookieValue), "UTF-8");
                    } catch (Exception e) {
                        logger.error("failed to decode '" + cookieValue + "' as"
                                + " a base64 string", e);
                        flashMessage = cookieValue;
                    }
                }
                if (last.size() == 0) {
                    last = new LinkedHashMap<String, String>();
                }
                this.last.put(name, flashMessage);
                Cookie cookie = new Cookie(cookies[i].getName(), "");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                invocation.getResponse().addCookie(cookie);
                if (logger.isDebugEnabled()) {
                    logger.debug("found flash message:" + name + "=" + flashMessage);
                }
            }
        }
    }

    public void writeNewMessages() {
        if (logger.isDebugEnabled()) {
            logger.debug("writeNextMessages");
        }
        HttpServletResponse response = invocation.getResponse();
        List<String> responseCookies = null;
        for (Map.Entry<String, String> entry : next.entrySet()) {
            if (responseCookies == null) {
                responseCookies = new ArrayList<String>(next.size());
            }
            String cookieValue;
            if (entry.getValue() == null) {
                cookieValue = "";
            } else {
                try {
                    cookieValue = base64.encodeToString(entry.getValue().getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new Error(e);
                }
            }
            Cookie cookie = new Cookie(cookiePrefix + entry.getKey(), cookieValue);
            cookie.setPath("/");
            // cookie.setMaxAge(1);
            response.addCookie(cookie);
            responseCookies.add(cookie.getName());
            if (logger.isDebugEnabled()) {
                logger.debug("write flash cookie:" + cookie.getName() + "=" + cookie.getValue());
            }
        }
        for (Map.Entry<String, String> entry : last.entrySet()) {
            if (responseCookies == null || !responseCookies.contains(entry.getKey())) {
                Cookie c = new Cookie(entry.getKey(), null);
                c.setMaxAge(0);
                c.setPath("/");
                response.addCookie(c);
                if (logger.isDebugEnabled()) {
                    logger.debug("delete flash cookie:" + c.getName() + "=" + c.getValue());
                }
            }
        }
    }

    @Override
    public boolean contains(String name) {
        readLastMessages();
        return last.containsKey(name);
    }

    @Override
    public String get(String name) {
        readLastMessages();
        return last.get(name);
    }

    @Override
    public Collection<String> getMessageNames() {
        readLastMessages();
        return last.keySet();
    }

    @Override
    public Map<String, String> getMessages() {
        return Collections.unmodifiableMap(last);
    }

    @Override
    public Flash add(String name, String flashMessage) {
        Assert.notNull(name, "Flash attribute name must not be null");
        flashMessage = PlaceHolderUtils.resolve(flashMessage, invocation);
        if (next.size() == 0) {
            next = new LinkedHashMap<String, String>(2);
        }
        next.put(name, flashMessage);
        if (logger.isDebugEnabled()) {
            logger.debug("add flashMessage: " + name + "=" + flashMessage);
        }
        return this;
    }

    @Override
    public Collection<String> getNewMessageNames() {
        return next.keySet();
    }

    @Override
    public Map<String, String> getNewMessages() {
        return Collections.unmodifiableMap(next);
    }

}

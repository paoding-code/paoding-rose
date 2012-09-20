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
package net.paoding.rose.scanning;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class LoadScope {

    // controllers->com.yourcompany.yourapp
    // applicationContext->com.yourcampany.yourapp
    // ...
    private Map<String, String[]> load = new HashMap<String, String[]>();

    /**
     * 通过一个一个开发者设置的字符串，创建一个LoadScope对象。
     * 如果在loadScope中没有指定componetType的，使用defType作为他的componetType.
     * <p>
     * loadScopeString: componetConf [; componetConf]*<br>
     * componetConf: [componetType = ] componetConfValue<br>
     * componetType: 'controllers' | 'applicationContext' | 'messages' |
     * '*' <br>
     * componetConfValue: package [, packages]*<br>
     * 
     * @param loadScopeString
     * @param defType
     */
    public LoadScope(String loadScopeString, String defType) {
        init(loadScopeString, defType);
    }

    public String[] getScope(String componentType) {
        String[] scope = this.load.get(componentType);
        if (scope == null) {
            scope = this.load.get("*");
        }
        return scope;
    }

    private void init(String loadScope, String defType) {
        if (StringUtils.isBlank(loadScope) || "*".equals(loadScope)) {
            return;
        }
        loadScope = loadScope.trim();
        String[] componetConfs = StringUtils.split(loadScope, ";");
        for (String componetConf : componetConfs) {
            if (StringUtils.isBlank(loadScope)) {
                continue;
            }
            // 代表"controllers=com.renren.xoa, com.renren.yourapp"串
            componetConf = componetConf.trim();
            int componetTypeIndex;
            String componetType = defType; // 代表"controllers", "applicationContext", "dao", "messages", "*"等串
            String componetConfValue = componetConf;
            if ((componetTypeIndex = componetConf.indexOf('=')) != -1) {
                componetType = componetConf.substring(0, componetTypeIndex).trim();
                componetConfValue = componetConf.substring(componetTypeIndex + 1).trim();
            }
            if (componetType.startsWith("!")) {
                componetType = componetType.substring(1);
            } else {
                componetConfValue = componetConfValue + ", net.paoding.rose";
            }
            String[] packages = StringUtils.split(componetConfValue, ", \t\n\r\0");//都好和\t之间有一个空格
            this.load.put(componetType, packages);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String[]> componetConf : load.entrySet()) {
            String componetType = componetConf.getKey();
            String componetConfValue[] = componetConf.getValue();
            sb.append(componetType).append("=");
            for (String value : componetConfValue) {
                sb.append(value).append(";");
            }
            if (componetConfValue.length > 0) {
                sb.setLength(sb.length() - 1);
            }
        }
        return super.toString();
    }
}

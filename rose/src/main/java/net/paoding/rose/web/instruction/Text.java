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
package net.paoding.rose.web.instruction;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class Text implements InstructionHelper {

    public static TextInstruction text(Object value) {
        TextInstruction instruction = new TextInstruction();
        String text = (value == null) ? "" : value.toString();
        if (text.length() > 0) {
            if (!(value instanceof CharSequence)) {
                if (value.getClass().getName().equalsIgnoreCase("JSONObject")) {
                    text = "json:" + text;
                }
            }
        }
        instruction.text(text);
        return instruction;
    }
}

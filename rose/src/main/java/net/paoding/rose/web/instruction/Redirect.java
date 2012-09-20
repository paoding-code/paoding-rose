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
public class Redirect implements InstructionHelper {

    public static RedirectInstruction location(String path) {
        RedirectInstruction instruction = new RedirectInstruction();
        instruction.location(path);
        return instruction;
    }

    public static RedirectInstruction module(String module) {
        RedirectInstruction instruction = new RedirectInstruction();
        instruction.module(module);
        return instruction;
    }

    public static RedirectInstruction controller(String controller) {
        RedirectInstruction instruction = new RedirectInstruction();
        instruction.controller(controller);
        return instruction;
    }

    public static RedirectInstruction action(String action) {
        RedirectInstruction instruction = new RedirectInstruction();
        instruction.action(action);
        return instruction;
    }
}

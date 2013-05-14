/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.statement;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

/**
 * 
 * @author qieqie
 * 
 */
public class DefaultInterpreterFactory implements InterpreterFactory {

    private Interpreter[] interpreters = new Interpreter[] { new SystemInterpreter() };

    public DefaultInterpreterFactory() {
    }

    public DefaultInterpreterFactory(Interpreter[] interpreters) {
        for (Interpreter interpreter : interpreters) {
            this.addInterpreter(interpreter);
        }
    }

    public DefaultInterpreterFactory(List<Interpreter> interpreters) {
        for (Interpreter interpreter : interpreters) {
            this.addInterpreter(interpreter);
        }
    }

    public synchronized void addInterpreter(Interpreter interpreter) {
        if (!ArrayUtils.contains(this.interpreters, interpreter)) {
            Interpreter[] interpreters = Arrays.copyOf(this.interpreters,
                    this.interpreters.length + 1);
            interpreters[this.interpreters.length] = interpreter;
            Arrays.sort(interpreters, new InterpreterComparator());
            this.interpreters = interpreters;
        }
    }

    @Override
    public Interpreter[] getInterpreters(StatementMetaData metaData) {
        return interpreters;
    }

}

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

import java.util.Comparator;

import org.springframework.core.annotation.Order;

/**
 * 
 * @author qieqie
 * 
 */
public class InterpreterComparator implements Comparator<Interpreter> {

    @Override
    public int compare(Interpreter thees, Interpreter that) {
        Order thessOrder = thees.getClass().getAnnotation(Order.class);
        Order thatOrder = that.getClass().getAnnotation(Order.class);
        int thessValue = thessOrder == null ? 0 : thessOrder.value();
        int thatValue = thatOrder == null ? 0 : thatOrder.value();
        return thessValue - thatValue;
    }
}

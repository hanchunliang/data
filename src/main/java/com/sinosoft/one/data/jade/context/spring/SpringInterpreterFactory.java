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
package com.sinosoft.one.data.jade.context.spring;

import com.sinosoft.one.data.jade.statement.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * 
 * @author qieqie
 * 
 */
public class SpringInterpreterFactory implements InterpreterFactory, ApplicationContextAware {

    private DefaultInterpreterFactory interpreterFactory;

    private ListableBeanFactory beanFactory;

    public SpringInterpreterFactory() {
    }

    public SpringInterpreterFactory(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = applicationContext;
    }


    public Interpreter[] getInterpreters(StatementMetaData metaData) {
        if (interpreterFactory == null) {
            init();
        }
        return interpreterFactory.getInterpreters(metaData);
    }

    private void init() {
        synchronized (this) {
            if (interpreterFactory == null) {
                @SuppressWarnings("unchecked")
                Map<String, Interpreter> map = beanFactory.getBeansOfType(Interpreter.class);
                ArrayList<Interpreter> interpreters = new ArrayList<Interpreter>(map.values());
                Collections.sort(interpreters, new InterpreterComparator());
                interpreterFactory = new DefaultInterpreterFactory(interpreters);
            }
        }
    }

}
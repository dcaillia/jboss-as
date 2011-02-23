/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.weld.webtier.jsf;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.jboss.weld.el.WeldELContextListener;

/**
 * @author pmuir
 *
 */
public class WeldApplication extends ForwardingApplication {

    private static class AdjustableELResolver extends ForwardingELResolver {

        private ELResolver delegate;

        public void setDelegate(ELResolver delegate) {
            this.delegate = delegate;
        }

        @Override
        protected ELResolver delegate() {
            return delegate;
        }
    }

    private final Application application;
    private volatile ExpressionFactory expressionFactory;
    private AdjustableELResolver elResolver;
    private volatile boolean initialized;

    public WeldApplication(Application application) {
        this.application = application;
        application.addELContextListener(new WeldELContextListener());
        elResolver = new AdjustableELResolver();
        elResolver.setDelegate(new DummyELResolver());
        application.addELResolver(elResolver);
    }

    private void init() {
        if (!initialized && beanManager() != null) {
            elResolver.setDelegate(beanManager().getELResolver());
            initialized = true;
        }
    }

    @Override
    protected Application delegate() {
        init();
        return application;
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        init();
        // may be improved for thread safety, but right now the only risk is to invoke wrapExpressionFactory
        // multiple times for concurrent threads. This is ok, as the call is
        if (expressionFactory == null) {
            expressionFactory = beanManager().wrapExpressionFactory(application.getExpressionFactory());
        }
        return expressionFactory;
    }

    private static BeanManager beanManager() {
        if (FacesContext.getCurrentInstance() != null
                && FacesContext.getCurrentInstance().getExternalContext().getContext() instanceof ServletContext) {
            ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
                    .getContext();
            return (BeanManager) servletContext.getAttribute(BeanManager.class.getName());
        } else {
            return null;
        }
    }

}
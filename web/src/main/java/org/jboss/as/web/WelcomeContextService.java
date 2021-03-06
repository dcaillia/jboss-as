/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.web;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomcat.InstanceManager;
import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.as.web.deployment.WebCtxLoader;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import javax.naming.NamingException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;

/**
 * A service starting a welcome web context driven by simple static content.
 *
 * @author Jason T. Greene
 */
class WelcomeContextService implements Service<Context> {

    private static final Logger log = Logger.getLogger("org.jboss.web");
    private final StandardContext context;
    private final InjectedValue<String> pathInjector = new InjectedValue<String>();
    private final InjectedValue<Host> hostInjector = new InjectedValue<Host>();


    public WelcomeContextService() {
        this.context = new StandardContext();
    }

    /** {@inheritDoc} */
    public synchronized void start(StartContext startContext) throws StartException {
            try {
                context.setPath("");
                context.addLifecycleListener(new ContextConfig());
                context.setDocBase(pathInjector.getValue() + File.separatorChar + "welcome-content");

                final Loader loader = new WebCtxLoader(this.getClass().getClassLoader());
                loader.setContainer(hostInjector.getValue());
                context.setLoader(loader);
                context.setInstanceManager(new LocalInstanceManager());

                context.setReplaceWelcomeFiles(true);
                context.addWelcomeFile("index.html");

                Wrapper wrapper = context.createWrapper();
                wrapper.setName("DefaultServlet");
                wrapper.setServletClass("org.apache.catalina.servlets.DefaultServlet");
                context.addChild(wrapper);

                context.addServletMapping("/", "DefaultServlet");
                context.addMimeMapping("html", "text/html");
                context.addMimeMapping("jpg", "image/jpeg");

                hostInjector.getValue().addChild(context);
                context.create();
            } catch (Exception e) {
                throw new StartException("failed to create context", e);
            }
            try {
                context.start();
            } catch (LifecycleException e) {
                throw new StartException("failed to start context", e);
            }
    }

    /** {@inheritDoc} */
    public synchronized void stop(StopContext stopContext) {
        try {
            hostInjector.getValue().removeChild(context);
            context.stop();
        } catch (LifecycleException e) {
            log.error("exception while stopping context", e);
        }
        try {
            context.destroy();
        } catch (Exception e) {
            log.error("exception while destroying context", e);
        }
    }

    /** {@inheritDoc} */
    public synchronized Context getValue() throws IllegalStateException {
        final Context context = this.context;
        if (context == null) {
            throw new IllegalStateException();
        }
        return context;
    }

    public InjectedValue<String> getPathInjector() {
        return pathInjector;
    }

     public InjectedValue<Host> getHostInjector() {
        return hostInjector;
    }

    private static class LocalInstanceManager implements InstanceManager {
        @Override
        public Object newInstance(String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
            return Class.forName(className).newInstance();
        }

        @Override
        public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
            return Class.forName(fqcn, false, classLoader).newInstance();
        }

        @Override
        public Object newInstance(Class<?> c) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
            return c.newInstance();
        }

        @Override
        public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
            throw new IllegalStateException();
        }

        @Override
        public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
        }
    }
}

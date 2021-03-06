/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.as.web.deployment.component;

import org.jboss.as.ee.component.BasicComponent;
import org.jboss.as.ee.component.BasicComponentCreateService;
import org.jboss.as.ee.component.BasicComponentInstance;
import org.jboss.as.naming.ManagedReference;
import org.jboss.invocation.Interceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of {@link org.jboss.as.ee.component.Component} for web components
 *
 * @author Stuart Douglas
 */
public class WebComponent extends BasicComponent {

    /**
     * Construct a new instance.
     *
     * @param componentCreateService the component configuration
     */
    public WebComponent(final BasicComponentCreateService componentCreateService) {
        super(componentCreateService);
    }

    @Override
    protected BasicComponentInstance instantiateComponentInstance(AtomicReference<ManagedReference> instanceReference, Interceptor preDestroyInterceptor, Map<Method, Interceptor> methodInterceptors) {
        return new WebComponentInstance(this, instanceReference, preDestroyInterceptor, methodInterceptors);
    }

    //    /** {@inheritDoc} */
//    @Override
//    public Interceptor createClientInterceptor(final Class<?> viewClass) {
//        return null; //not applicable
//    }
//
//    @Override
//    public Interceptor createClientInterceptor(Class<?> view, Serializable sessionId) {
//        return null;  //not applicable
//    }
}

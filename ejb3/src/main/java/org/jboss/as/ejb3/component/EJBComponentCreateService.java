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

package org.jboss.as.ejb3.component;

import org.jboss.as.ee.component.BasicComponentCreateService;
import org.jboss.as.ee.component.ComponentConfiguration;
import org.jboss.as.ee.component.ViewConfiguration;
import org.jboss.as.ejb3.deployment.EjbJarConfiguration;

import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jaikiran Pai
 */
public class EJBComponentCreateService extends BasicComponentCreateService {

    private final ConcurrentMap<MethodIntf, ConcurrentMap<String, ConcurrentMap<ArrayKey, TransactionAttributeType>>> txAttrs;

    private final TransactionManagementType transactionManagementType;

    private final EjbJarConfiguration ejbJarConfiguration;

    /**
     * Construct a new instance.
     *
     * @param componentConfiguration the component configuration
     */
    public EJBComponentCreateService(final ComponentConfiguration componentConfiguration, final EjbJarConfiguration ejbJarConfiguration) {
        super(componentConfiguration);

        this.ejbJarConfiguration = ejbJarConfiguration;

        EJBComponentDescription ejbComponentDescription = (EJBComponentDescription) componentConfiguration.getComponentDescription();
        this.transactionManagementType = ejbComponentDescription.getTransactionManagementType();

        // CMTTx
        if (transactionManagementType.equals(TransactionManagementType.CONTAINER)) {
            // slurp some memory
            this.txAttrs = new ConcurrentHashMap<MethodIntf, ConcurrentMap<String, ConcurrentMap<ArrayKey, TransactionAttributeType>>>();
        } else {
            this.txAttrs = null;
        }
        List<ViewConfiguration> views = componentConfiguration.getViews();
        if (views != null) {
            for (ViewConfiguration view : views) {
                final MethodIntf viewType = ejbComponentDescription.getMethodIntf(view.getViewClass().getName());
                for (Method method : view.getProxyFactory().getCachedMethods()) {
                    this.processTxAttr(ejbComponentDescription, viewType, method);
                }
            }
        }
        // FIXME: TODO: a temporary measure until EJBTHREE-2120 is fully resolved, let's create tx attribute map
        // for the component methods. Once the issue is resolved, we should get rid of this block and just rely on setting
        // up the tx attributes only for the views exposed by this component
        Set<Method> componentMethods = componentConfiguration.getDefinedComponentMethods();
        if (componentMethods != null) {
            for (Method method : componentMethods) {
                this.processTxAttr(ejbComponentDescription, MethodIntf.BEAN, method);
            }
        }
    }

    ConcurrentMap<MethodIntf, ConcurrentMap<String, ConcurrentMap<ArrayKey, TransactionAttributeType>>> getTxAttrs() {
        return txAttrs;
    }

    TransactionManagementType getTransactionManagementType() {
        return transactionManagementType;
    }

    EjbJarConfiguration getEjbJarConfiguration() {
        return this.ejbJarConfiguration;
    }

    private void processTxAttr(final EJBComponentDescription ejbComponentDescription, final MethodIntf methodIntf, final Method method) {
        if (this.getTransactionManagementType().equals(TransactionManagementType.BEAN)) {
            // it's a BMT bean
            return;
        }

        String methodName = method.getName();
        TransactionAttributeType txAttr = ejbComponentDescription.getTransactionAttribute(methodIntf, methodName, toString(method.getParameterTypes()));

        ConcurrentMap<String, ConcurrentMap<ArrayKey, TransactionAttributeType>> perMethodIntf = this.txAttrs.get(methodIntf);
        if (perMethodIntf == null) {
            perMethodIntf = new ConcurrentHashMap<String, ConcurrentMap<ArrayKey, TransactionAttributeType>>();
            this.txAttrs.put(methodIntf, perMethodIntf);
        }
        ConcurrentMap<ArrayKey, TransactionAttributeType> perMethod = perMethodIntf.get(methodName);
        if (perMethod == null) {
            perMethod = new ConcurrentHashMap<ArrayKey, TransactionAttributeType>();
            perMethodIntf.put(methodName, perMethod);
        }
        perMethod.put(new ArrayKey((Object[]) method.getParameterTypes()), txAttr);
    }

    private static String[] toString(Class<?>[] a) {
        final String[] result = new String[a.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = a[i].getName();
        }
        return result;
    }


}

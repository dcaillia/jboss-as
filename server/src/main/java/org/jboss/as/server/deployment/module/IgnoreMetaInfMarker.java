/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.as.server.deployment.module;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;

/**
 * Marker that indicates that the contents of a resource roots META-INF directory should be ignored.
 *
 * @author Stuart Douglas
 *
 */
public class IgnoreMetaInfMarker {

    private static final Logger log = Logger.getLogger("org.jboss.as.server.deployment.module");

    private static AttachmentKey<Boolean> IGNORE_META_INF = AttachmentKey.create(Boolean.class);

    public static void mark(ResourceRoot root) {
        VirtualFile file = root.getRoot().getChild("META-INF");
        if(file.exists()) {
            log.warnf("META-INF directory %s ignored as it is not a valid location for META-INF", file.getPathName());
        }
        root.putAttachment(IGNORE_META_INF, true);
    }

    public static boolean isIgnoreMetaInf(ResourceRoot resourceRoot) {
        final Boolean res = resourceRoot.getAttachment(IGNORE_META_INF);
        return res != null && res;
    }

}

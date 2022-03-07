/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.heap;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

import com.oracle.svm.core.thread.VMOperation;
import org.graalvm.compiler.api.replacements.Fold;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.hosted.Feature;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.annotate.UnknownObjectField;
import com.oracle.svm.core.thread.VMOperation.SystemEffect;

/**
 * This class is used to ensure that all VM operation names live in the image heap. It also keeps
 * track of all VM operation names that are present in the image.
 *
 * The VM operation names are user facing as they are for example used in JFR events.
 */
public final class VMOperationInfos {
    @Platforms(Platform.HOSTED_ONLY.class) private static final HashMap<VMOperationKey, VMOperationInfo> HostedMap = new HashMap<>();
    @UnknownObjectField(types = VMOperationInfos[].class) static String[] Names = new String[0];

    @Fold
    public static VMOperationInfos singleton() {
        return ImageSingletons.lookup(VMOperationInfos.class);
    }

    /**
     * All arguments must be constants. If this isn't the case, then the image build will fail
     * because the HostedMap would need to be reachable at run-time.
     */
    @Fold
    public static VMOperationInfo get(Class<? extends VMOperation> clazz, String name, SystemEffect systemEffect) {
        synchronized (HostedMap) {
            VMOperationKey key = new VMOperationKey(clazz, name);
            VMOperationInfo result = HostedMap.get(key);
            if (result == null) {
                // Generate a unique id per (clazz, name) tuple.
                int id = HostedMap.size();
                result = new VMOperationInfo(id, name, systemEffect);
                HostedMap.put(key, result);
            }
            return result;
        }
    }

    @Platforms(Platform.HOSTED_ONLY.class)
    public static void cacheNames() {
        Names = new String[HostedMap.size()];
        for (Entry<VMOperationKey, VMOperationInfo> entry : HostedMap.entrySet()) {
            Names[entry.getValue().getId()] = entry.getKey().getName();
        }
    }

    public static String[] getNames() {
        return Names;
    }

    private static class VMOperationKey {
        private final Class<? extends VMOperation> clazz;
        private final String name;

        VMOperationKey(Class<? extends VMOperation> clazz, String name) {
            this.clazz = clazz;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            VMOperationKey that = (VMOperationKey) o;
            return Objects.equals(clazz, that.clazz) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, name);
        }
    }
}

@AutomaticFeature
class VMOperationNamesFeatures implements Feature {
    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        ImageSingletons.add(VMOperationInfos.class, new VMOperationInfos());
    }

    @Override
    public void beforeCompilation(BeforeCompilationAccess access) {
        VMOperationInfos.cacheNames();
        access.registerAsImmutable(VMOperationInfos.getNames());
    }
}

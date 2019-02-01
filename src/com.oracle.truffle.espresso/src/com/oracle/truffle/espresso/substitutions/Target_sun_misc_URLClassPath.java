/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

package com.oracle.truffle.espresso.substitutions;

import com.oracle.truffle.espresso.runtime.StaticObject;

import java.net.URL;

@EspressoSubstitutions
public final class Target_sun_misc_URLClassPath {
    /**
     * These ... new JVM_ functions uses hotspot internals to improve sun.misc.URLClassPath search
     * time, a hack! http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2015-December/034337.html
     */
    @SuppressWarnings("unused")
    @Substitution
    public static @Host(URL[].class) StaticObject getLookupCacheURLs(@Host(ClassLoader.class) StaticObject classLoader) {
        return StaticObject.NULL;
    }
}

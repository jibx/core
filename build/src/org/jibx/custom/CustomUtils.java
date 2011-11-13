/*
 * Copyright (c) 2007, Dennis M. Sosnoski All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.custom;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Support methods used by customization code.
 * 
 * @author Dennis M. Sosnoski
 */
public class CustomUtils
{
    /**
     * Utility method to add an array of names to a set, ignoring case. All the supplied names are converted to lower
     * case before they are added to the set.
     * 
     * @param names (<code>null</code> if none)
     * @param set base set of names (<code>null</code> if none)
     * @return name set (<code>null</code> if none)
     */
    public static Set addNoCaseSet(String[] names, Set set) {
        if (names == null) {
            return set;
        } else {
            if (set == null) {
                set = new HashSet();
            }
            for (int i = 0; i < names.length; i++) {
                set.add(names[i].toLowerCase());
            }
            return set;
        }
    }

    /**
     * Utility method to build a set from an array of names, ignoring case. All the supplied names are converted to
     * lower case before they are added to the set.
     * 
     * @param names (<code>null</code> if none)
     * @return name set (<code>null</code> if name array also <code>null</code>, otherwise non-<code>null</code>)
     */
    public static Set noCaseNameSet(String[] names) {
        if (names == null) {
            return null;
        } else {
            HashSet set = new HashSet();
            for (int i = 0; i < names.length; i++) {
                set.add(names[i].toLowerCase());
            }
            return set;
        }
    }

    /**
     * Utility method to build a set from an array of names.
     * 
     * @param names (<code>null</code> if none)
     * @return name set (<code>null</code> if name array also <code>null</code>, otherwise non-<code>null</code>)
     */
    public static Set nameSet(String[] names) {
        if (names == null) {
            return null;
        } else {
            HashSet set = new HashSet();
            for (int i = 0; i < names.length; i++) {
                set.add(names[i]);
            }
            return set;
        }
    }

    /**
     * Clean directory by recursively deleting children.
     *
     * @param dir directory to be cleaned
     */
    public static void clean(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    clean(file);
                }
                file.delete();
            }
        }
    }
}
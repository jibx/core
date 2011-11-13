/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.dom.AST;
import org.jibx.binding.model.BindingOrganizer;
import org.jibx.binding.model.BindingHolder;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.util.UniqueNameSet;

/**
 * Information for a package to be included in code generated from schema.
 * 
 * @author Dennis M. Sosnoski
 */
public class PackageHolder
{
    /** Full package name (dot-separated form). */
    private final String m_packageName;
    
    /** Target directory for code generation. */
    private final File m_generateDirectory;
    
    /** Information for parent package. */
    private final PackageHolder m_parentPackage;
    
    /** Set of class names used in package. */
    private final UniqueNameSet m_nameSet;
    
    /** List of top-level classes in package. */
    private final ArrayList m_classes;
    
    /** List of all classes in package, including inner classes. */
    private final ArrayList m_allClasses;
    
    /** Number of subpackages of this package. */
    private int m_subpackageCount;
    
    /**
     * Constructor.
     * 
     * @param name full package name (dot-separated form)
     * @param dir target directory for code generation (<code>null</code> if skipping code generation)
     * @param parent parent package information
     */
    public PackageHolder(String name, File dir, PackageHolder parent) {
        m_packageName = name;
        m_generateDirectory = dir;
        m_parentPackage = parent;
        if (m_parentPackage != null) {
            m_parentPackage.m_subpackageCount++;
        }
        m_nameSet = new UniqueNameSet();
        m_classes = new ArrayList();
        m_allClasses = new ArrayList();
    }
    
    /**
     * Get generate directory.
     * 
     * @return generate directory
     */
    public File getGenerateDirectory() {
        return m_generateDirectory;
    }
    
    /**
     * Get parent package.
     * 
     * @return parent package
     */
    public PackageHolder getParent() {
        return m_parentPackage;
    }
    
    /**
     * Get fully-qualified package name.
     * 
     * @return name
     */
    public String getName() {
        return m_packageName;
    }
    
    /**
     * Get the number of top-level classes in package.
     *
     * @return count
     */
    public int getTopClassCount() {
        return m_classes.size();
    }
    
    /**
     * Get the total number of classes (including inner classes) in package.
     *
     * @return count
     */
    public int getClassCount() {
        return m_allClasses.size();
    }
    
    /**
     * Get the number of subpackages created for this package.
     *
     * @return count
     */
    public int getSubpackageCount() {
        return m_subpackageCount;
    }
    
    /**
     * Add class to package.
     * 
     * @param name preferred name for class
     * @param nconv name converter for class
     * @param decorators class decorators
     * @param inner use inner classes for substructures
     * @param enumer enumeration class flag
     * @param holder binding holder
     * @return class generator
     */
    public ClassHolder addClass(String name, NameConverter nconv, ClassDecorator[] decorators, boolean inner,
        boolean enumer, BindingHolder holder) {
        String actual = m_nameSet.add(name);
        ClassHolder def = enumer ? new EnumerationClassHolder(actual, actual, this, holder, nconv, decorators, inner) :
            (ClassHolder)new StructureClassHolder(actual, actual, this, holder, nconv, decorators, inner);
        m_classes.add(def);
        m_allClasses.add(def);
        return def;
    }
    
    /**
     * Add derived class to package. This method is only used when top-level classes are being used for substructures.
     * 
     * @param name preferred name for class
     * @param base base class name
     * @param nconv name converter for class
     * @param decorators class decorators
     * @param enumer enumeration class flag
     * @param holder binding holder
     * @return class generator
     */
    public ClassHolder addClass(String name, String base, NameConverter nconv, ClassDecorator[] decorators,
        boolean enumer, BindingHolder holder) {
        ClassHolder def = enumer ?
            new EnumerationClassHolder(m_nameSet.add(name), base, this, holder, nconv, decorators, false) :
            (ClassHolder)new StructureClassHolder(m_nameSet.add(name), base, this, holder, nconv, decorators, false);
        m_classes.add(def);
        m_allClasses.add(def);
        return def;
    }
    
    /**
     * Add an inner class to package.
     *
     * @param clas
     */
    public void addInnerClass(IClassHolder clas) {
        m_allClasses.add(clas);
    }
    
    /**
     * Generate a specific class within this package. This first tests if the class has already been generated, and if
     * it has does nothing.
     *
     * @param verbose 
     * @param clasdata class data
     * @param ast
     */
    public void generate(boolean verbose, TypeData clasdata, AST ast) {
        if (!clasdata.isPregenerated()) {
            ClassHolder clashold = (ClassHolder)clasdata;
            if (!clashold.isGenerated()) {
                
                // make sure the generate directory exists
                if (m_generateDirectory != null) {
                    m_generateDirectory.mkdirs();
                    if (!m_generateDirectory.exists()) {
                        throw new IllegalStateException("Unable to create directory " + m_generateDirectory.getAbsolutePath());
                    }
                }
                
                // run the actual class generation
                SourceBuilder builder = new SourceBuilder(ast, this, clashold.getName(), clashold.getImports());
                clashold.generate(verbose, builder);
                builder.finish(verbose);
                
            }
        }
    }
    
    /**
     * Generate this package.
     * 
     * @param verbose 
     * @param ast
     * @param directory binding directory
     */
    public void generate(boolean verbose, AST ast, BindingOrganizer directory) {
        for (int i = 0; i < m_classes.size(); i++) {
            ClassHolder clashold = (ClassHolder)m_classes.get(i);
            generate(verbose, clashold, ast);
        }
    }

    /**
     * Get the field information for every class in this package. The returned pairs have the fully-qualified class name
     * as the key, and the array of ordered field name and type string pairs as the values. This method is only
     * meaningful after the class has been generated.
     *
     * @return class field information
     */
    public StringObjectPair[] getClassFields() {
        StringObjectPair[] pairs = new StringObjectPair[m_allClasses.size()];
        for (int i = 0; i < m_allClasses.size(); i++) {
            ClassHolder clas = (ClassHolder)m_allClasses.get(i);
            pairs[i] = new StringObjectPair(clas.getFullName(), clas.getBuilder().getSortedFields());
        }
        Arrays.sort(pairs);
        return pairs;
    }
}
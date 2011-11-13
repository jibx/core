/*
 * Copyright (c) 2006-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.runtime.QName;
import org.jibx.schema.ISkipElements;
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupElement;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.GroupElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.util.InsertionOrderedMap;

/**
 * Tracks the schema validation state. This includes order-dependent state information collected while walking the tree
 * structure of a schema model. Collects all errors and warnings and maintains a summary of the severity of the problems
 * found. For ease of use, this also wraps the schema name register with convenience methods for validation.
 * 
 * TODO: separate out a generalized base class and move the base out of the schema package
 * 
 * @author Dennis M. Sosnoski
 */
public class ValidationContext extends SchemaContextTracker implements ISkipElements
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ValidationContext.class.getName());
    
    /** Map from identifier to schema. */
    private Map m_idSchemaMap;
    
    /** Map from namespace URI to schema. */
    private Map m_namespaceSchemaMap;
    
    /** Set of namespaces with multiple schemas. */
    private Set m_duplicateNamespaces;
    
    /** Number of unimplementeds reported. */
    private int m_unimplementedCount;
    
    /** Number of warnings reported. */
    private int m_warningCount;
    
    /** Number of errors reported. */
    private int m_errorCount;
    
    /** Number of fatals reported. */
    private int m_fatalCount;
    
    /** List of problem items reported by validation. */
    private ArrayList m_problemList;
    
    /** Set of elements to be skipped in walking tree. */
    private Set m_skipSet;
    
    /**
     * Constructor.
     */
    public ValidationContext() {
        m_idSchemaMap = new InsertionOrderedMap();
        m_namespaceSchemaMap = new HashMap();
        m_duplicateNamespaces = new HashSet();
        m_problemList = new ArrayList();
        m_skipSet = new HashSet();
    }
    
    /**
     * Reset context for reuse.
     */
    public void reset() {
        m_idSchemaMap.clear();
        m_problemList.clear();
        m_skipSet.clear();
        clearTraversed();
    }
    
    /**
     * Get schema element by target namespace. The target namespace must be unique.
     * 
     * @param uri unique namespace URI
     * @return schema, or <code>null</code> if not loaded or non-unique namespace
     */
    public SchemaElement getSchemaByNamespace(String uri) {
        return (SchemaElement)m_namespaceSchemaMap.get(uri);
    }
    
    /**
     * Get schema element by identifier. This uses the unique schema identifier to locate a loaded schema instance.
     * 
     * @param id
     * @return schema, or <code>null</code> if not loaded
     */
    public SchemaElement getSchemaById(String id) {
        return (SchemaElement)m_idSchemaMap.get(id);
    }
    
    /**
     * Get iterator for all schemas defined in this context.
     * 
     * @return iterator
     */
    public Iterator iterateSchemas() {
        return m_idSchemaMap.values().iterator();
    }
    
    /**
     * Add schema element with identifier. TODO: is the namespace handling sufficient?
     * 
     * @param id
     * @param schema
     */
    public void setSchema(String id, SchemaElement schema) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Setting schema with id=" + id);
        }
        m_idSchemaMap.put(id, schema);
        String tns = schema.getTargetNamespace();
        if (tns != null) {
            if (m_namespaceSchemaMap.containsKey(tns)) {
                m_namespaceSchemaMap.remove(tns);
                m_duplicateNamespaces.add(tns);
            }
            if (!m_duplicateNamespaces.contains(tns)) {
                m_namespaceSchemaMap.put(tns, schema);
            }
        }
    }
    
    /**
     * Get the number of schemas processed by this context.
     * 
     * @return count
     */
    public int getSchemaCount() {
        return m_idSchemaMap.size();
    }
    
    /**
     * Get number of unimplemented feature problems reported.
     * 
     * @return unimplemented feature problem count
     */
    public int getUnimplementedCount() {
        return m_unimplementedCount;
    }
    
    /**
     * Get number of warning problems reported.
     * 
     * @return warning problem count
     */
    public int getWarningCount() {
        return m_warningCount;
    }
    
    /**
     * Get number of error problems reported.
     * 
     * @return error problem count
     */
    public int getErrorCount() {
        return m_errorCount;
    }
    
    /**
     * Get number of fatal problems reported.
     * 
     * @return fatal problem count
     */
    public int getFatalCount() {
        return m_fatalCount;
    }
    
    /**
     * Register global attribute in the current schema definition. If the name has already been registered this creates
     * an error for the new definition.
     * 
     * @param qname name
     * @param def attribute definition
     */
    public void registerAttribute(QName qname, AttributeElement def) {
        AttributeElement dupl = m_nameRegister.registerAttribute(qname, def);
        if (dupl != null) {
            addError("Duplicate name " + qname, def);
        }
    }
    
    /**
     * Register global attribute group in the current schema definition. If the name has already been registered this
     * creates an error for the new definition.
     * 
     * @param qname name
     * @param def attribute definition
     */
    public void registerAttributeGroup(QName qname, AttributeGroupElement def) {
        AttributeGroupElement dupl = m_nameRegister.registerAttributeGroup(qname, def);
        if (dupl != null) {
            addError("Duplicate name " + qname, def);
        }
    }
    
    /**
     * Register global element in the current schema definition. If the name has already been registered this creates an
     * error for the new definition.
     * 
     * @param qname name
     * @param def element definition
     */
    public void registerElement(QName qname, ElementElement def) {
        ElementElement dupl = m_nameRegister.registerElement(qname, def);
        if (dupl != null) {
            addError("Duplicate name " + qname, def);
        }
    }
    
    /**
     * Register global group in the current schema definition. If the name has already been registered this creates an
     * error for the new definition.
     * 
     * @param qname name
     * @param def attribute definition
     */
    public void registerGroup(QName qname, GroupElement def) {
        GroupElement dupl = m_nameRegister.registerGroup(qname, def);
        if (dupl != null) {
            addError("Duplicate name " + qname, def);
        }
    }
    
    /**
     * Register global type in the current schema definition. If the name has already been registered this creates an
     * error for the new definition.
     * 
     * @param qname name
     * @param def attribute definition
     */
    public void registerType(QName qname, CommonTypeDefinition def) {
        CommonTypeDefinition dupl = m_nameRegister.registerType(qname, def);
        if (dupl != null) {
            addError("Duplicate name " + qname, def);
        }
    }
    
    /**
     * Find global attribute by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public AttributeElement findAttribute(QName qname) {
        return m_nameRegister.findAttribute(qname);
    }
    
    /**
     * Find attribute group by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public AttributeGroupElement findAttributeGroup(QName qname) {
        return m_nameRegister.findAttributeGroup(qname);
    }
    
    /**
     * Find global element by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public ElementElement findElement(QName qname) {
        return m_nameRegister.findElement(qname);
    }
    
    /**
     * Find group by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public GroupElement findGroup(QName qname) {
        return m_nameRegister.findGroup(qname);
    }
    
    /**
     * Find global type by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public CommonTypeDefinition findType(QName qname) {
        return m_nameRegister.findType(qname);
    }
    
    /**
     * Add unimplemented feature item for current element. Adds an unimplemented feature item to the problem list,
     * reporting a schema feature which is not supported but does not prevent allows reasonable operation.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     */
    public void addUnimplemented(String msg, Object obj) {
        addProblem(new ValidationProblem(ValidationProblem.UNIMPLEMENTED_LEVEL, msg, obj));
    }
    
    /**
     * Add warning item. Adds a warning item to the problem list, which is a possible problem that still allows
     * reasonable operation.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     */
    public void addWarning(String msg, Object obj) {
        addProblem(new ValidationProblem(ValidationProblem.WARNING_LEVEL, msg, obj));
    }
    
    /**
     * Add error item. Adds an error item to the problem list, which is a definite problem that still allows validation
     * to proceed.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     * @return <code>true</code> if to continue validation, <code>false</code> if not
     */
    public boolean addError(String msg, Object obj) {
        addProblem(new ValidationProblem(ValidationProblem.ERROR_LEVEL, msg, obj));
        return true;
    }
    
    /**
     * Add fatal item. Adds a fatal item to the problem list, which is a severe problem that blocks further validation
     * within the tree branch involved. The object associated with a fatal error should always be an element.
     * 
     * @param msg problem description
     * @param obj source object for validation error (should be an element)
     */
    public void addFatal(String msg, Object obj) {
        addProblem(new ValidationProblem(ValidationProblem.FATAL_LEVEL, msg, obj));
    }
    
    /**
     * Add problem report. The problem is added and counted as appropriate.
     * 
     * @param problem details of problem report
     */
    public void addProblem(ValidationProblem problem) {
        m_problemList.add(problem);
        switch (problem.getSeverity())
        {
            
            case ValidationProblem.ERROR_LEVEL:
                m_errorCount++;
                break;
            
            case ValidationProblem.FATAL_LEVEL:
                m_fatalCount++;
                addSkip(problem.getComponent());
                break;
            
            case ValidationProblem.UNIMPLEMENTED_LEVEL:
                m_unimplementedCount++;
                break;
            
            case ValidationProblem.WARNING_LEVEL:
                m_warningCount++;
                break;
            
        }
    }
    
    /**
     * Get list of problems.
     * 
     * @return problem list
     */
    public ArrayList getProblems() {
        return m_problemList;
    }
    
    /**
     * Add element to set to be skipped.
     * 
     * @param skip
     */
    protected void addSkip(Object skip) {
        if (skip instanceof SchemaBase) {
            m_skipSet.add(skip);
        }
    }
    
    /**
     * Report problems using handler. This clears the problem list after they've been reported, to avoid multiple
     * reports of the same problems.
     * 
     * @param handler problem handler
     * @return <code>true</code> if one or more errors, <code>false</code> if not
     */
    public boolean reportProblems(ProblemHandler handler) {
        ArrayList probs = getProblems();
        boolean error = false;
        if (probs.size() > 0) {
            for (int j = 0; j < probs.size(); j++) {
                ValidationProblem prob = (ValidationProblem)probs.get(j);
                switch (prob.getSeverity()) {
                    
                    case ValidationProblem.UNIMPLEMENTED_LEVEL:
                        handler.handleUnimplemented(prob);
                        break;
                        
                    case ValidationProblem.WARNING_LEVEL:
                        handler.handleWarning(prob);
                        break;
                        
                    case ValidationProblem.ERROR_LEVEL:
                        handler.handleError(prob);
                        error = true;
                        break;
                        
                    case ValidationProblem.FATAL_LEVEL:
                        handler.handleFatal(prob);
                        error = true;
                        break;
                        
                }
            }
        }
        probs.clear();
        return error;
    }
    
    //
    // ISkipElements implementation
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ISkipElements#isSkipped(java.lang.Object)
     */
    public boolean isSkipped(Object obj) {
        return m_skipSet.contains(obj);
    }
}
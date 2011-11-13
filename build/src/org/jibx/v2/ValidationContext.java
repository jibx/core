/*
 * Copyright (c) 2006-2007, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jibx.runtime.JiBXException;
import org.jibx.schema.ISkipElements;
import org.jibx.schema.elements.SchemaBase;

/**
 * Tracks the schema validation state. This includes order-dependent state information collected while walking the tree
 * structure of a schema model. Collects all errors and warnings and maintains a summary of the severity of the problems
 * found. For ease of use, this also wraps the schema name register with convenience methods for validation.
 * 
 * @author Dennis M. Sosnoski
 */
public class ValidationContext implements ISkipElements
{
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
    
    /** Flag for errors to be ignored. */
    private boolean m_continueOnError;
    
    /**
     * Constructor.
     */
    public ValidationContext() {
        m_problemList = new ArrayList();
        m_skipSet = new HashSet();
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
     * Add warning item. Adds a warning item to the problem list, which is a possible problem that still allows
     * reasonable operation.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     * @throws JiBXException on unrecoverable error
     */
    public void addWarning(String msg, Object obj) throws JiBXException {
        addProblem(new ValidationProblem(ValidationProblem.WARNING_LEVEL, msg, obj));
    }
    
    /**
     * Add error item. Adds an error item to the problem list, which is a definite problem that still allows validation
     * to proceed.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     * @return <code>true</code> if to continue validation, <code>false</code> if not
     * @throws JiBXException on unrecoverable error
     */
    public boolean addError(String msg, Object obj) throws JiBXException {
        addProblem(new ValidationProblem(ValidationProblem.ERROR_LEVEL, msg, obj));
        return true;
    }
    
    /**
     * Add fatal item. Adds a fatal item to the problem list, which is a severe problem that blocks further validation
     * within the tree branch involved. The object associated with a fatal error should always be an element.
     * 
     * @param msg problem description
     * @param obj source object for validation error (should be an element)
     * @throws JiBXException on unrecoverable error
     */
    public void addFatal(String msg, Object obj) throws JiBXException {
        addProblem(new ValidationProblem(ValidationProblem.FATAL_LEVEL, msg, obj));
    }
    
    /**
     * Add problem report. The problem is added and counted as appropriate.
     * 
     * @param problem details of problem report
     * @throws JiBXException on unrecoverable error
     */
    public void addProblem(ValidationProblem problem) throws JiBXException {
        m_problemList.add(problem);
        switch (problem.getSeverity())
        {
            
            case ValidationProblem.ERROR_LEVEL:
                m_errorCount++;
                break;
            
            case ValidationProblem.FATAL_LEVEL:
                m_fatalCount++;
                addSkip(problem.getComponent());
                throw new JiBXException("Unrecoverable error " + problem.getDescription());
            
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
    
    // so what kinds of errors need to be handled? there's the obvious, like missing element when unmarshalling, data
    //  conversion error, etc. - but how specific do I want to make the handling? can I come up with a small number of
    //  specific errors that I can add directly to the context? missing element, for instance, should give the actual
    //  element name. do I want to build in XPath-like navigation, too? could kind of do this when marshalling or
    //  unmarshalling, since I have the path information, but that leaves the issue of position in a collection. should
    //  I optionally implement a generic collection approach for the generated classes? to support full XPath I'd need
    //  to create methods to handle attribute and child element lookup by name, which might be not too difficult.
    public void handleMissingElement() throws JiBXException {
        if (m_continueOnError) {
        }
    }
}
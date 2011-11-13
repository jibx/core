/*
Copyright (c) 2004-2007, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.binding.model;

import java.util.ArrayList;

import org.jibx.util.IClass;
import org.jibx.util.IClassLocator;

/**
 * Tracks the validation state. This includes the current validation phase, as
 * well as order-dependent state information collected while walking the tree
 * structure of a binding model. Collects all errors and warnings and maintains
 * a summary of the severity of the problems found.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
 
public class ValidationContext extends TreeContext
{
    /** Number of warnings reported. */
    private int m_warningCount;
    
    /** Number of errors reported. */
    private int m_errorCount;
    
    /** Number of fatals reported. */
    private int m_fatalCount;
    
    /** List of problem items reported by validation. */
    private ArrayList m_problemList;
    
    /**
     * Constructor.
     * 
     * @param iloc class locator
     */
    public ValidationContext(IClassLocator iloc) {
        super(iloc);
        m_problemList = new ArrayList();
    }
    
    /**
     * Peek current element of hierarchy, if any. This variation should be used
     * for the actual error handling, in case an error occurs during the
     * initialization of the outer context definitions (canned definitions).
     * 
     * @return current element, or <code>null</code> if none
     */
    private ElementBase safePeekElement() {
        try {
            return peekElement();
        } catch (ArrayIndexOutOfBoundsException e) {
            // just in case there is no context element
            return null;
        }
    }
    
    /**
     * Prevalidate binding model tree. This calls the prevalidate method for
     * each element in the tree, in preorder traversal order.
     * 
     * @param root binding node of tree to be prevalidated
     */
    public void prevalidate(BindingElement root) {
        PrevalidationVisitor visitor = new PrevalidationVisitor();
        tourTree(root, visitor);
    }
    
    /**
     * Validate binding model tree. This calls the validate method for each
     * element in the tree, in postorder traversal order.
     * 
     * @param root binding node of tree to be prevalidated
     */
    public void validate(BindingElement root) {
        ValidationVisitor visitor = new ValidationVisitor();
        tourTree(root, visitor);
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
     * Add warning item for current element. Adds a warning item to the problem
     * list, which is a possible problem that still allows reasonable operation.
     * This form of the call can only be used during a tree tour being
     * controlled by this context.
     * 
     * @param msg problem description
     */
    public void addWarning(String msg) {
        addWarning(msg, safePeekElement());
    }
    
    /**
     * Add warning item. Adds a warning item to the problem list, which is a
     * possible problem that still allows reasonable operation.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     */
    public void addWarning(String msg, Object obj) {
        addProblem(new ValidationProblem
            (ValidationProblem.WARNING_LEVEL, msg, obj));
    }
    
    /**
     * Add error item for current element. Adds an error item to the problem
     * list, which is a definite problem that still allows validation to
     * proceed. This form of the call can only be used during a tree tour being
     * controlled by this context.
     * 
     * @param msg problem description
     */
    public void addError(String msg) {
        addError(msg, safePeekElement());
    }
    
    /**
     * Add error item. Adds an error item to the problem list, which is a
     * definite problem that still allows validation to proceed.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     */
    public void addError(String msg, Object obj) {
        addProblem(new ValidationProblem
            (ValidationProblem.ERROR_LEVEL, msg, obj));
    }
    
    /**
     * Add fatal item for current element. Adds a fatal item to the problem
     * list, which is a severe problem that blocks further validation within the
     * tree branch involved. This form of the call can only be used during a
     * tree tour being controlled by this context.
     * 
     * @param msg problem description
     */
    public void addFatal(String msg) {
        addFatal(msg, safePeekElement());
    }
    
    /**
     * Add fatal item. Adds a fatal item to the problem list, which is a severe
     * problem that blocks further validation within the tree branch involved.
     * The object associated with a fatal error should always be an element.
     * 
     * @param msg problem description
     * @param obj source object for validation error (should be an element)
     */
    public void addFatal(String msg, Object obj) {
        addProblem(new ValidationProblem
            (ValidationProblem.FATAL_LEVEL, msg, obj));
    }
    
    /**
     * Add problem report. The problem is added and counted as appropriate.
     * 
     * @param problem details of problem report
     */
    public void addProblem(ValidationProblem problem) {
        m_problemList.add(problem);
        switch (problem.getSeverity()) {
            
            case ValidationProblem.ERROR_LEVEL:
                m_errorCount++;
                break;
                
            case ValidationProblem.FATAL_LEVEL:
                m_fatalCount++;
                addSkip(problem.getComponent());
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
     * Get class information. Finds a class by name using the class locator
     * configured by the environment code. This overrides the base class
     * implementation in order to report failures that result in exceptions.
     *
     * @param name fully-qualified name of class to be found
     * @return class information, or <code>null</code> if class not found
     */
    public IClass getClassInfo(String name) {
        try {
            return super.getClassInfo(name);
        } catch (Exception e) {
            addFatal(e.getMessage());
            return null;
        }
    }
    
    /**
     * Inner class for handling prevalidation. This visitor implementation just
     * calls the {@link org.jibx.binding.model#prevalidate} method for each
     * element visited in preorder.
     */
    protected class PrevalidationVisitor extends ModelVisitor
    {
        /* (non-Javadoc)
         * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.ElementBase)
         */
        public boolean visit(ElementBase node) {
            try {
                node.prevalidate(ValidationContext.this);
            } catch (Throwable t) {
                addFatal("Error during validation: " + t.getMessage());
                t.printStackTrace();
                return false;
            }
            return true;
        }
    }
    
    /**
     * Inner class for handling validation. This visitor implementation just
     * calls the {@link org.jibx.binding.model#validate} method for each
     * element visited in postorder.
     */
    protected class ValidationVisitor extends ModelVisitor
    {
        /* (non-Javadoc)
         * @see org.jibx.binding.model.ModelVisitor#exit(org.jibx.binding.model.ElementBase)
         */
        public void exit(ElementBase node) {
            try {
                node.validate(ValidationContext.this);
            } catch (Throwable t) {
                addFatal("Error during validation: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }
}
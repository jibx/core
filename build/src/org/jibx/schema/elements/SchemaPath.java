/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema.elements;

import java.util.ArrayList;
import java.util.List;

import org.jibx.runtime.QName;
import org.jibx.schema.INamed;
import org.jibx.schema.IReference;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.LazyList;

/**
 * Path specification within a schema definition. This implements simple XPath-like expressions, consisting of any
 * number of path components given as element names or '*' for any element or '**' for any nesting of elements, along
 * with optional position number or name attribute predicates in square brackets.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaPath
{
    /** Single element wildcard step. */
    private static final StepBase WILDCARD_ELEMENT_STEP = new StepBase() {
        
        public boolean isRepeating() {
            return false;
        }
        
        public boolean match(OpenAttrBase elem) {
            return true;
        }
        
        public int position() {
            return -1;
        }
        
    };
    
    /** Nesteing element wildcard step. */
    private static final StepBase WILDCARD_NESTING_STEP = new StepBase() {
        
        public boolean isRepeating() {
            return true;
        }
        
        public boolean match(OpenAttrBase elem) {
            return true;
        }
        
        public int position() {
            return -1;
        }
        
    };
    
    /** Source object for path expression. */
    private final Object m_sourceObject;
    
    /** Validation context used for reporting errors. */
    private final ValidationContext m_validationContext;
    
    /** Path steps. */
    private StepBase[] m_steps;
    
    /**
     * Constructor.
     * 
     * @param obj source object for expression
     * @param vctx validation context
     */
    private SchemaPath(Object obj, ValidationContext vctx) {
        m_sourceObject = obj;
        m_validationContext = vctx;
    }

    /**
     * Validate a name attribute value.
     *
     * @param nameattr name value
     * @return <code>true</code> if valid, <code>false</code> if not
     */
    private boolean validateName(String nameattr) {
        for (int i = 0; i < nameattr.length(); i++) {
            char chr = nameattr.charAt(i);
            if (!Character.isLetter(chr) && chr != '.' && chr != '_' && chr != '-' && 
                (i <= 0 || !Character.isDigit(chr))) {
                m_validationContext.addError("Invalid path expression name predicate '" + nameattr + '\'',
                    m_sourceObject);
                return false;
            }
        }
        return true;
    }

    /**
     * Validate and convert a position value.
     *
     * @param postext position text
     * @return position value (strictly positive), or <code>-1</code> if error
     */
    private int convertPosition(String postext) {
        
        // first check that all characters are digits (for better error message)
        for (int i = 0; i < postext.length(); i++) {
            char chr = postext.charAt(i);
            if (chr < '0' || chr > '9') {
                if (i == 0) {
                    m_validationContext.addError("Unknown path expression predicate '" + postext + '\'',
                        m_sourceObject);
                } else {
                    m_validationContext.addError("Illegal character in path expression position predicate '" + postext +
                        "' (must be digits only)", m_sourceObject);
                }
                return -1;
            }
        }
        
        // convert the actual position value
        int position = -1;
        try {
            position = Integer.parseInt(postext);
            if (position <= 0) {
                m_validationContext.addError("Path expression position predicate value must be >= 1", m_sourceObject);
            }
        } catch (NumberFormatException e) {
            m_validationContext.addError("Error parsing position predicate in path expression", m_sourceObject);
        }
        return position;
    }
    
    /**
     * Build a path step.
     * 
     * @param step expression
     * @return constructed step, or <code>null</code> if error
     */
    private StepBase buildPathStep(String step) {
        if ("*".equals(step)) {
            return WILDCARD_ELEMENT_STEP;
        } else if ("**".equals(step)) {
            return WILDCARD_NESTING_STEP;
        } else {
            
            // check if there's a predicate present
            boolean valid = true;
            String elemname = null;
            String nameattr = null;
            String postext = null;
            int split = step.indexOf('[');
            if (split >= 0) {
                
                // split off element name as part before predicate start
                elemname = step.substring(0, split);
                step = step.substring(split + 1);
                
                // make sure there's a matching predicate end
                split = step.indexOf(']');
                if (split >= 0) {
                    
                    // check type of predicate
                    String clause = step.substring(0, split).trim();
                    if (clause.startsWith("@name=")) {
                        nameattr = clause.substring(6);
                        valid = validateName(nameattr);
                    } else {
                        postext = clause;
                    }
                    
                    // check for a second predicate
                    step = step.substring(split + 1);
                    if (step.length() > 0) {
                        
                        // second predicate must be position
                        int end = step.length() - 1;
                        if (step.charAt(0) == '[' && step.charAt(end) == ']') {
                            clause = step.substring(1, end).trim();
                            if (postext == null) {
                                postext = clause;
                            } else {
                                m_validationContext.addError("Multiple predicates only allowed in path expression " +
                                    "with [@name=xxx] as first predicate", m_sourceObject);
                                valid = false;
                            }
                        } else {
                            m_validationContext.addError("Invalid predicate in path expression", m_sourceObject);
                            valid = false;
                        }
                        
                    }
                } else {
                    m_validationContext.addError("Invalid predicate in path expression", m_sourceObject);
                    valid = false;
                }
            } else {
                elemname = step;
            }
            
            // decode the position predicate
            int position = -1;
            if (valid && postext != null) {
                position = convertPosition(postext);
                valid = position > 0;
            }
            
            // return constructed step if syntax is valid
            if (valid) {
                return new PathStep(elemname, position, nameattr);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Find matches for expression starting from a supplied schema element.
     *
     * @param offset current path step offset
     * @param end ending match list offset
     * @param base starting element for match
     * @param matches elements matching expression
     */
    private void match(int offset, int end, OpenAttrBase base, ArrayList matches) {
        LazyList childs = base.getChildrenWritable();
        StepBase step = m_steps[offset];
        int steppos = step.position();
        int position = 0;
        for (int i = 0; i < childs.size(); i++) {
            OpenAttrBase child = (OpenAttrBase)childs.get(i);
            if (step.match(child)) {
                if (steppos <= 0 || steppos == ++position) {
                    if (offset == end) {
                        matches.add(child);
                    } else {
                        match(offset + 1, end, child, matches);
                    }
                    if (step.isRepeating()) {
                        match(offset, end, child, matches);
                    }
                    if (steppos > 0) {
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Get length of this path (minimum number of nested elements).
     *
     * @return path length
     */
    public int getPathLength() {
        return m_steps.length;
    }
    
    /**
     * Check if the first path step is a wildcard.
     *
     * @return <code>true</code> if wildcard, <code>false</code> if not
     */
    public boolean isWildStart() {
        return m_steps[0] == WILDCARD_ELEMENT_STEP || m_steps[0] == WILDCARD_NESTING_STEP;
    }
    
    /**
     * Find any number of matches for subexpression starting from a supplied schema element annotation.
     *
     * @param first starting path step index
     * @param last ending path step index
     * @param base starting element for match
     * @return matching element, or <code>null</code> if error
     */
    public List partialMatchMultiple(int first, int last, OpenAttrBase base) {
        ArrayList matches = new ArrayList();
        match(first, last, base, matches);
        return matches;
    }
    
    /**
     * Find unique match for subexpression starting from a supplied schema element annotation. An error is reported if
     * no match is found, or if multiple matches are found.
     *
     * @param first starting path step index
     * @param last ending path step index
     * @param base starting element for match
     * @return matching element, or <code>null</code> if error
     */
    public OpenAttrBase partialMatchUnique(int first, int last, OpenAttrBase base) {
        List matches = partialMatchMultiple(first, last, base);
        OpenAttrBase match = null;
        if (matches.size() == 0) {
            m_validationContext.addError("No match found for path expression", m_sourceObject);
        } else if (matches.size() > 1) {
            m_validationContext.addError("Multiple matches found for path expression", m_sourceObject);
        } else {
            match = (OpenAttrBase)matches.get(0);
        }
        return match;
    }
    
    /**
     * Find unique match for expression starting from a supplied schema element annotation. An error is reported if no
     * match is found, or if multiple matches are found.
     *
     * @param base starting element for match
     * @return matching element, or <code>null</code> if error
     */
    public OpenAttrBase matchUnique(OpenAttrBase base) {
        return partialMatchUnique(0, m_steps.length-1, base);
    }
     
    /**
     * Build a path. If a path expression is supplied, the final path step in the expression must either not use an
     * element name, or the element name must match the actual element supplied.
     * 
     * @param path expression (<code>null</code> if none)
     * @param elemname element name for final step in path
     * @param nameattr name attribute (applied to final step in path, <code>null</code> if none)
     * @param postext position (applied to final step in path, <code>null</code> if none)
     * @param obj object defining the path
     * @param vctx validation context
     * @return constructed path, or <code>null</code> if error
     */
    public static SchemaPath buildPath(String path, String elemname, String nameattr, String postext, Object obj,
        ValidationContext vctx) {
        
        // start by handling supplied values for final step predicates
        SchemaPath inst = new SchemaPath(obj, vctx);
        boolean valid = true;
        int position = -1;
        if (postext != null) {
            position = inst.convertPosition(postext);
            if (position < 0) {
                valid = false;
            }
        }
        if (nameattr != null && !inst.validateName(nameattr)) {
            valid = false;
        }
        
        // check for only last step involved
        StepBase[] steps;
        if (path == null) {
            steps = new StepBase[] { new PathStep(elemname, position, nameattr) };
        } else {
            
            // path supplied, process each step
            ArrayList steplist = new ArrayList();
            int base = 0;
            int split;
            while ((split = path.indexOf('/', base)) >= 0) {
                StepBase step = inst.buildPathStep(path.substring(base, split));
                base = split + 1;
                if (step == null) {
                    valid = false;
                } else {
                    steplist.add(step);
                }
            }
            
            // check the final step
            String steptext = path.substring(base);
            if ("**".equals(steptext)) {
                
                // add wildcard step, then create separate final step from attributes and name
                steplist.add(WILDCARD_NESTING_STEP);
                
            } else {
                
                // strip element name from last path step, if present
                if (steptext.startsWith(elemname)) {
                    steptext = steptext.substring(elemname.length());
                }
                
                // build the last path step from path
                StepBase step = inst.buildPathStep(elemname + steptext);
                if (step instanceof PathStep) {
                    
                    // make sure all components match specified values
                    PathStep laststep = (PathStep)step;
                    if (!elemname.equals(laststep.m_elementName)) {
                        vctx.addError("Last path step must use no element name, or the specified element name", obj);
                        valid = false;
                    }
                    if (position <= 0) {
                        position = laststep.m_position;
                    } else if (laststep.m_position > 0 && position != laststep.m_position) {
                        vctx.addError("Position must not be used in last path step, or must match specified value",
                            obj);
                        valid = false;
                    }
                    if (nameattr == null) {
                        nameattr = laststep.m_name;
                    } else if (laststep.m_name != null && !nameattr.equals(laststep.m_name)) {
                        vctx.addError("Name atribute must not be used in last path step, or must match specified value",
                            obj);
                        valid = false;
                    }
                    
                }
            }
            
            // add generated final step combining specified values with those from path
            steplist.add(new PathStep(elemname, position, nameattr));
            steps = (StepBase[])steplist.toArray(new StepBase[steplist.size()]);
        }
        
        // return configured instance if valid
        if (valid) {
            inst.m_steps = steps;
            return inst;
        } else {
            return null;
        }
    }
   
    public abstract static class StepBase
    {
        public abstract boolean match(OpenAttrBase elem);
        
        public abstract boolean isRepeating();
        
        public abstract int position();
    }
    
    public static class PathStep extends StepBase
    {
        private final String m_elementName;
        
        private final int m_position;
        
        private final String m_name;
        
        protected PathStep(String elemname, int position, String name) {
            m_elementName = elemname;
            m_position = position;
            m_name = name;
        }
        
        public boolean isRepeating() {
            return false;
        }
        
        public boolean match(OpenAttrBase elem) {
            if (elem.name().equals(m_elementName)) {
                if (m_name == null) {
                    return true;
                } else if (elem instanceof INamed && m_name.equals(((INamed)elem).getName())) {
                    return true;
                } else if (elem instanceof IReference) {
                    QName ref = ((IReference)elem).getRef();
                    return ref != null && m_name.equals(ref.getName());
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        public int position() {
            return m_position;
        }
    }
}
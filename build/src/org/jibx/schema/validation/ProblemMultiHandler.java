/*
 * Copyright (c) 2010, Dennis M. Sosnoski. All rights reserved.
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
import java.util.List;

/**
 * Problem handler which delegates to any number of other handlers.
 */
public class ProblemMultiHandler implements ProblemHandler
{
    /** List of delegate handlers. */
    private List m_handlers = new ArrayList();
    
    /**
     * Add handler to delegate list.
     * 
     * @param handler
     */
    public void addHandler(ProblemHandler handler) {
        m_handlers.add(handler);
    }
    
    /**
     * Handle unimplemented feature.
     * 
     * @param prob
     */
    public void handleUnimplemented(ValidationProblem prob) {
        for (int i = 0; i < m_handlers.size(); i++) {
            ((ProblemHandler)m_handlers.get(i)).handleUnimplemented(prob);
        }
    }

    /**
     * Handle warning.
     * 
     * @param prob
     */
    public void handleWarning(ValidationProblem prob) {
        for (int i = 0; i < m_handlers.size(); i++) {
            ((ProblemHandler)m_handlers.get(i)).handleWarning(prob);
        }
    }
    
    /**
     * Handle error.
     * 
     * @param prob
     */
    public void handleError(ValidationProblem prob) {
        for (int i = 0; i < m_handlers.size(); i++) {
            ((ProblemHandler)m_handlers.get(i)).handleError(prob);
        }
    }
    
    /**
     * Handle fatal.
     * 
     * @param prob
     */
    public void handleFatal(ValidationProblem prob) {
        for (int i = 0; i < m_handlers.size(); i++) {
            ((ProblemHandler)m_handlers.get(i)).handleFatal(prob);
        }
    }
    
    /**
     * Report progress information.
     * 
     * @param msg progress information
     */
    public void report(String msg) {
        for (int i = 0; i < m_handlers.size(); i++) {
            ((ProblemHandler)m_handlers.get(i)).report(msg);
        }
    }
    
    /**
     * Terminate processing.
     * 
     * @param msg message reporting why processing is being terminated.
     */
    public void terminate(String msg) {
        for (int i = 0; i < m_handlers.size(); i++) {
            ((ProblemHandler)m_handlers.get(i)).terminate(msg);
        }
    }
    
    /**
     * Terminate processing.
     * 
     * @param msg message reporting why processing is being terminated.
     * @param thr throwable with problem details
     */
    public void terminate(String msg, Throwable thr) {
        for (int i = 0; i < m_handlers.size(); i++) {
            ((ProblemHandler)m_handlers.get(i)).terminate(msg, thr);
        }
    }
}
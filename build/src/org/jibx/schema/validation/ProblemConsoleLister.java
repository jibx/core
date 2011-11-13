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

/**
 * Handler to list problems found in validation to console.
 */
public class ProblemConsoleLister implements ProblemHandler
{
    /**
     * Handle unimplemented feature.
     * 
     * @param prob
     */
    public void handleUnimplemented(ValidationProblem prob) {
        System.out.print("Unimplemented: ");
        System.out.println(prob.getDescription());
    }

    /**
     * Handle warning.
     * 
     * @param prob
     */
    public void handleWarning(ValidationProblem prob) {
        System.out.print("Warning: ");
        System.out.println(prob.getDescription());
    }
    
    /**
     * Handle error.
     * 
     * @param prob
     */
    public void handleError(ValidationProblem prob) {
        System.out.print("Error: ");
        System.out.println(prob.getDescription());
    }
    
    /**
     * Handle fatal.
     * 
     * @param prob
     */
    public void handleFatal(ValidationProblem prob) {
        System.out.print("Fatal: ");
        System.out.println(prob.getDescription());
    }
    
    /**
     * Report progress information.
     * 
     * @param msg progress information
     */
    public void report(String msg) {
        System.out.println(msg);
    }
    
    /**
     * Terminate processing.
     * 
     * @param msg message reporting why processing is being terminated
     */
    public void terminate(String msg) {
        System.err.println(msg);
    }
    
    /**
     * Terminate processing.
     * 
     * @param msg message reporting why processing is being terminated
     * @param thr throwable with problem details
     */
    public void terminate(String msg, Throwable thr) {
        System.err.println(msg + ": " + thr.getMessage());
    }
}
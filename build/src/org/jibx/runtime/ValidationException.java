/*
Copyright (c) 2002-2007, Dennis M. Sosnoski.
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

package org.jibx.runtime;

/**
 * Validation exception class. This is used for marshalling and unmarshalling
 * errors that relate to data content.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class ValidationException extends RecoverableException
{
    /**
     * Constructor from message.
     * 
     * @param msg message describing the exception condition
     */
     
    public ValidationException(String msg) {
        super(msg);
    }
    
    /**
     * Constructor from message and wrapped exception.
     * 
     * @param msg message describing the exception condition
     * @param root exception which caused this exception
     */
    
    public ValidationException(String msg, Throwable root) {
        super(msg, root);
    }
    
    /**
     * Constructor from message and validation object.
     * 
     * @param msg message describing the exception condition
     * @param obj source object for validation error
     */
     
    public ValidationException(String msg, Object obj) {
        super(addDescription(msg, obj));
    }
    
    /**
     * Constructor from message, wrapped exception, and validation object.
     * 
     * @param msg message describing the exception condition
     * @param root exception which caused this exception
     * @param obj source object for validation error
     */
    
    public ValidationException(String msg, Throwable root, Object obj) {
        super(addDescription(msg, obj), root);
    }
    
    /**
     * Constructor from message, validation object, and unmarshalling context.
     * 
     * @param msg message describing the exception condition
     * @param obj source object for validation error
     * @param ctx context used for unmarshalling
     */
    
    public ValidationException(String msg, Object obj,
        IUnmarshallingContext ctx) {
        super(addDescription(msg, obj));
    }
    
    /**
     * Get description information for a validation object. For an unmarshalled
     * object with source references available this returns the source position
     * description. Otherwise, it returns the result of a {@link
     * java.lang.Object#toString} method call.
     * 
     * @param obj source object for validation error
     * @return object description text
     */
     
    public static String describe(Object obj) {
        if (obj instanceof ITrackSource) {
            ITrackSource track = (ITrackSource)obj;
            if (track.jibx_getColumnNumber() != 0 || track.jibx_getLineNumber() != 0 || track.jibx_getDocumentName() != null) {
                StringBuffer text = new StringBuffer();
                text.append("(line ");
                text.append(track.jibx_getLineNumber());
                text.append(", col ");
                text.append(track.jibx_getColumnNumber());
                if (track.jibx_getDocumentName() != null) {
                    text.append(", in ");
                    text.append(track.jibx_getDocumentName());
                }
                text.append(')');
                return text.toString();
            }
        }
        return "(source unknown)";
    }
    
    /**
     * Add description information for a validation object to message. This just
     * appends the result of a {@link #describe} call to the supplied message,
     * with some appropriate formatting.
     * 
     * @param msg base message text
     * @param obj source object for validation error
     * @return message with object description appended
     */
     
    public static String addDescription(String msg, Object obj) {
        return msg + " (" + describe(obj) + ")";
    }
    
    /**
     * Get exception description.
     * 
     * @return message describing the exception condition
     */
     
    public String getMessage() {
        return super.getMessage();
    }
}
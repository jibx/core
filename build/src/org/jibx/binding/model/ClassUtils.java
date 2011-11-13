/*
Copyright (c) 2004-2008, Dennis M. Sosnoski.
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

import java.util.HashMap;

import org.jibx.util.IClass;
import org.jibx.util.IClassItem;

/**
 * Utilities for working with class, field, or method information.
 *
 * @author Dennis M. Sosnoski
 */
public class ClassUtils
{
    /** Map for primitive type signature variants. */
    private static HashMap s_variantMap = new HashMap();
    
    /** Map for signatures corresponding to class names. */
    private static HashMap s_signatureMap = new HashMap();
    
    static {
        
        // initialize primitive type variants
        s_variantMap.put("boolean", new String[] { "Z" });
        s_variantMap.put("byte", new String[] { "B", "S", "I" });
        s_variantMap.put("char", new String[] { "C", "I" });
        s_variantMap.put("double", new String[] { "D" });
        s_variantMap.put("float", new String[] { "F" });
        s_variantMap.put("int", new String[] { "I" });
        s_variantMap.put("long", new String[] { "J" });
        s_variantMap.put("short", new String[] { "S", "I" });
        s_variantMap.put("void", new String[] { "V" });
        
        // initialize signatures for primitive types
        s_signatureMap.put("boolean", "Z");
        s_signatureMap.put("byte", "B");
        s_signatureMap.put("char", "C");
        s_signatureMap.put("double", "D");
        s_signatureMap.put("float", "F");
        s_signatureMap.put("int", "I");
        s_signatureMap.put("long", "J");
        s_signatureMap.put("short", "S");
        s_signatureMap.put("void", "V");
    }

	/**
	 * Check if type name is a primitive.
	 *
     * @param type 
	 * @return <code>true</code> if a primitive, <code>false</code> if not
	 */
	public static boolean isPrimitive(String type) {
		return s_variantMap.get(type) != null;
	}
	
	/**
	 * Get virtual method by fully qualified name. This splits the class
	 * name from the method name, finds the class, and then tries to find a
	 * matching method name in that class or a superclass.
	 *
	 * @param name fully qualified class and method name
	 * @param sigs possible method signatures
     * @param vctx validation context (used for class lookup)
	 * @return information for the method, or <code>null</code> if not found
	 */
	public static IClassItem findVirtualMethod(String name, String[] sigs,
        ValidationContext vctx) {
		
		// get the class containing the method
		int split = name.lastIndexOf('.');
		String cname = name.substring(0, split);
		String mname = name.substring(split+1);
		IClass iclas = vctx.getClassInfo(cname);
        if (iclas != null) {
            
            // find the method in class or superclass
            for (int i = 0; i < sigs.length; i++) {
                IClassItem method = iclas.getMethod(mname, sigs[i]);
                if (method != null) {
                    return method;
                }
            }
        }
		return null;
	}
	
	/**
	 * Get static method by fully qualified name. This splits the class
	 * name from the method name, finds the class, and then tries to find a
	 * matching method name in that class.
	 *
	 * @param name fully qualified class and method name
	 * @param sigs possible method signatures
     * @param vctx validation context (used for class lookup)
	 * @return information for the method, or <code>null</code> if not found
	 */
	public static IClassItem findStaticMethod(String name, String[] sigs,
        ValidationContext vctx) {
		
		// get the class containing the method
		int split = name.lastIndexOf('.');
		if (split > 0) {
    		String cname = name.substring(0, split);
    		String mname = name.substring(split+1);
    		IClass iclas = vctx.getClassInfo(cname);
            if (iclas != null) {
                
                // find the method in class or superclass
                for (int i = 0; i < sigs.length; i++) {
                    IClassItem method = iclas.getStaticMethod(mname, sigs[i]);
                    if (method != null) {
                        return method;
                    }
                }
            }
		}
		return null;
	}
	
	/**
	 * Get all variant signatures for a fully qualified class name. The
	 * returned array gives all signatures (for interfaces or classes) which
	 * instances of the class can match.
	 *
	 * @param name fully qualified class name
     * @param vctx validation context (used for class lookup)
	 * @return possible signature variations for instances of the class
	 */
	public static String[] getSignatureVariants(String name,
        ValidationContext vctx) {
		Object obj = s_variantMap.get(name);
		if (obj == null) {
			IClass iclas = vctx.getRequiredClassInfo(name);
			return iclas.getInstanceSigs();
		} else {
			return (String[])obj;
		}
	}
	
	/**
	 * Gets the signature string corresponding to a type. The base for the type
     * may be a primitive or class name, and may include trailing array
     * brackets.
	 *
	 * @param type type name
	 * @return signature string for type
	 */
	public static String getSignature(String type) {
        
        //. check if already built signature for this type
        String sig = (String)s_signatureMap.get(type);
        if (sig == null) {
            
            // check if this is an array type
            int dim = 0;
            int split = type.indexOf('[');
            if (split >= 0) {
                
                // count pairs of array brackets
                int mark = split;
                while ((type.length()-mark) >= 2) {
                    if (type.charAt(mark) == '[' ||
                        type.charAt(mark+1) == ']') {
                        dim++;
                        mark += 2;
                    } else {
                        throw new IllegalArgumentException
                            ("Invalid type name " + type);
                    }
                }
                
                // make sure only bracket pairs at end
                if (mark < type.length()) {
                    throw new IllegalArgumentException("Invalid type name " +
                        type);
                }
                
                // see if signature for base object type needs to be added
                String cname = type.substring(0, split);
                String base = (String)s_signatureMap.get(cname);
                if (base == null) {
                    
                    // add base type signature to map
                    base = "L" + cname.replace('.', '/') + ';';
                    s_signatureMap.put(cname, base);
                    
                }
                
                // prepend appropriate number of 
                StringBuffer buff = new StringBuffer(dim + base.length());
                for (int i = 0; i < dim; i++) {
                    buff.append('[');
                }
                buff.append(base);
                sig = buff.toString();
                    
            } else {
                
                // define signature for ordinary object type
                sig = "L" + type.replace('.', '/') + ';';
                
            }
            
            // add signature definition to map
            s_signatureMap.put(type, sig);
        }
        
        // return signature for type
        return sig;
    }
	
	/**
	 * Check if a value of one type can be directly assigned to another type.
	 * This is basically the equivalent of the instanceof operator, but with
	 * application to primitive types as well as object types.
	 *
	 * @param from fully qualified class name of initial type
	 * @param to fully qualified class name of assignment type
     * @param vctx validation context (used for class lookup)
	 * @return <code>true</code> if assignable, <code>false</code> if not
	 */
	public static boolean isAssignable(String from, String to,
        ValidationContext vctx) {
		
		// always assignable if the two are the same
		if (from.equals(to)) {
			return true;
		} else {
			
			// try direct lookup for primitive types
			Object fobj = s_variantMap.get(from);
			Object tobj = s_variantMap.get(to);
			if (fobj == null && tobj == null) {
				
				// find the actual class information
				IClass fclas = vctx.getRequiredClassInfo(from);
                IClass tclas = vctx.getRequiredClassInfo(to);
                
                // assignable if from type has to as a possible signature
                String[] sigs = fclas.getInstanceSigs();
                String match = tclas.getSignature();
                for (int i = 0; i < sigs.length; i++) {
                    if (match.equals(sigs[i])) {
                        return true;
                    }
                }
                return false;
				
			} else if (fobj != null && tobj != null) {
				
				// assignable if from type has to as a possible signature
				String[] fsigs = (String[])fobj;
				String[] tsigs = (String[])tobj;
				if (tsigs.length == 1) {
					for (int i = 0; i < fsigs.length; i++) {
						if (fsigs[i] == tsigs[0]) {
							return true;
						}
					}
				}
				return false;
				
			} else {
				
				// primitive and object types never assignable
				return false;
				
			}
		}
	}
}
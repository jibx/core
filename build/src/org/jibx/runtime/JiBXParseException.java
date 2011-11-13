/*
Copyright (c) 2008, Joshua Davies.
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
 * JiBX parsing exception class.  This subclass of JiBXException provides
 * additional details when a parsing error occurs such as what tag was
 * being parsed and what value caused the error.
 * 
 * @author Joshua Davies
 */
public class JiBXParseException extends JiBXException 
{
	private String m_value;
	private String m_namespace;
	private String m_tagName;
	
	/**
	 * Constructor from message.
	 * @param msg the throwers description of what's gone wrong.
	 * @param value the value which was unparseable (in string format).
	 */
	public JiBXParseException(String msg, String value)	{
		super(msg);
		m_value = value;
	}
	
	/**
	 * Constructor from message and wrapped exception.
	 * @param msg the throwers description of what's gone wrong.
	 * @param value the value which was unparseable (in string format).
     * @param root exception which caused this exception
	 */
	public JiBXParseException(String msg, String value, Throwable root)	{
		super(msg, root);
		m_value = value;
	}
	
    /**
     * Constructor from message, wrapped exception and tag name.
     * 
     * @param msg message describing the exception condition
     * @param value the value which was unparseable (in string format).
     * @param namespace the namespace (if any) associated with the tag.
     * @param tagName the name of the tag whose element caused the exception.
     * @param root exception which caused this exception
     */
	public JiBXParseException(String msg, String value, String namespace, String tagName, Throwable root)	{
		super(msg, root);
		m_value = value;
		m_namespace = namespace;
		m_tagName = tagName;
	}
	
	/**
	 * Add namespace detail to the exception.
	 * @param namespace the namespace of the offending tag.
	 */
	public void setNamespace(String namespace)	{
		m_namespace = namespace;
	}
	
	/**
	 * Add tag name detail to the exception.
	 * @param tagName the name of the offending tag.
	 */
	public void setTagName(String tagName)	{
		m_tagName = tagName;
	}
	
	/**
	 * Append useful parsing details onto the default message.
	 * @return the parent's message plus "caused by value" addendum.
	 */
	public String getMessage()	{
		return super.getMessage() + ", caused by value '" + m_value + "' for tag '" +
			( m_namespace == null ? "" : m_namespace + ":" ) + m_tagName + "'";
	}

	/**
	 * This is only used for testing purposes.
	 * @param obj what to compare against.
	 * @return true or false
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final JiBXParseException other = (JiBXParseException) obj;
		if (m_namespace == null) {
			if (other.m_namespace != null)
				return false;
		} else if (!m_namespace.equals(other.m_namespace))
			return false;
		if (m_tagName == null) {
			if (other.m_tagName != null)
				return false;
		} else if (!m_tagName.equals(other.m_tagName))
			return false;
		if (m_value == null) {
			if (other.m_value != null)
				return false;
		} else if (!m_value.equals(other.m_value))
			return false;
		return true;
	}
}

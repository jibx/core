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

import java.util.Arrays;

/**
 * Thrown when a "constrained" parsing exception is encountered (e.g. an
 * enumerated value).
 * 
 * @author Joshua Davies
 */
public class JiBXConstrainedParseException extends JiBXParseException {
	private String[] m_allowableValues;

	public JiBXConstrainedParseException(String msg, String value,
			String[] allowableValues) {
		super(msg, value);
		m_allowableValues = allowableValues;
	}

	public JiBXConstrainedParseException(String msg, String value,
			String[] allowableValues, String namespace, String tagName,
			Throwable root) {
		super(msg, value, namespace, tagName, root);
		m_allowableValues = allowableValues;
	}

	/**
	 * @return the default message with "constraint" text appended.
	 */
	public String getMessage() {
		StringBuffer constraint = new StringBuffer();
		constraint.append(".  Acceptable values are ");
		for (int i = 0; i < m_allowableValues.length; i++) {
			constraint.append("'" + m_allowableValues[i] + "'");
			if (i < (m_allowableValues.length - 1)) {
				constraint.append(", ");
			}
		}
		return super.getMessage() + constraint.toString() + ".";
	}

	/**
	 * Solely used by unit tests.
	 *
	 * @param obj what to compare against
	 * @return true or false
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final JiBXConstrainedParseException other = (JiBXConstrainedParseException) obj;
		if (!Arrays.equals(m_allowableValues, other.m_allowableValues))
			return false;
		return true;
	}
}

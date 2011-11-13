/*
Copyright (c) 2002,2003, Sosnoski Software Solutions, Inc.
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

package multiple;

/**
 * Carrier information.
 *
 * @author Dennis M. Sosnoski
 * @version 0.8
 */

public class CarrierBean
{
	private String m_ident;
	private String m_name;
	private String m_url;
	private int m_rating;
	
	public CarrierBean() {}
	public void setIdent(String ident) {
		m_ident = ident;
	}
	public String getIdent() {
		return m_ident;
	}
	public void setName(String name) {
		m_name = name;
	}
	public String getName() {
		return m_name;
	}
	public void setURL(String url) {
		m_url = url;
	}
	public String getURL() {
		return m_url;
	}
	public void setRating(int rating) {
		m_rating = rating;
	}
	public int getRating() {
		return m_rating;
	}
	public boolean equals(Object obj) {
		if (obj instanceof CarrierBean) {
			CarrierBean compare = (CarrierBean)obj;
			return Utils.equalStrings(m_ident, compare.m_ident) &&
				Utils.equalStrings(m_name, compare.m_name) &&
				Utils.equalStrings(m_url, compare.m_url) &&
				m_rating == compare.m_rating;
		} else {
			return false;
		}
	}
}

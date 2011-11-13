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

import java.util.Date;

import org.jibx.runtime.JiBXException;
import org.jibx.runtime.Utility;

/**
 * Flight identity information.
 *
 * @author Dennis M. Sosnoski
 * @version 0.8
 */

public class FlightIdentityBean
{
	private static Date s_defaultStartDate;
	private static Date s_defaultEndDate;
	{
		try {
			s_defaultStartDate = Utility.deserializeDate("2003-01-01");
			s_defaultEndDate = Utility.deserializeDate("2005-12-31");
		} catch (JiBXException ex) {
			s_defaultStartDate = s_defaultEndDate = new Date();
		}
	}
	
	private CarrierBean m_carrier;
    private String m_id;
	private int m_number;
	private double m_onTime;
	private Date m_startDate;
	private Date m_endDate;
	
	public FlightIdentityBean() {}
	public void setCarrier(CarrierBean carrier) {
		m_carrier = carrier;
	}
	public CarrierBean getCarrier() {
		return m_carrier;
	}
	public void setNumber(int number) {
		m_number = number;
	}
	public int getNumber() {
		return m_number;
	}
	public void preset() {
		m_onTime = 0.5D;
	}
	public void postComplete() {
		if (m_startDate == null) {
			m_startDate = s_defaultStartDate;
		}
		if (m_endDate == null) {
			m_endDate = s_defaultEndDate;
		}
	}
	public void preget() {
		m_onTime *= 1.5D;
	}
	public boolean equals(Object obj) {
		if (obj instanceof FlightBean) {
			FlightIdentityBean compare = (FlightIdentityBean)obj;
			return Utils.equalCarriers(m_carrier, compare.m_carrier) &&
				m_number == compare.m_number &&
				Utils.equalDates(m_startDate, compare.m_startDate) &&
				Utils.equalDates(m_endDate, compare.m_endDate);
		} else {
			return false;
		}
	}
}

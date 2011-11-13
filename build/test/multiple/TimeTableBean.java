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

import java.util.ArrayList;

/**
 * Flight timetable information.
 *
 * @author Dennis M. Sosnoski
 * @version 0.8
 */

public class TimeTableBean
{
	private ArrayList m_carriers;
	private ArrayList m_airports;
	private ArrayList m_routes;
	
	public TimeTableBean() {
		m_carriers = new ArrayList();
		m_airports = new ArrayList();
		m_routes = new ArrayList();
	}
	public ArrayList getCarriers() {
		return m_carriers;
	}
	public void addCarrier(CarrierBean carrier) {
		m_carriers.add(carrier);
	}
	public ArrayList getAirports() {
		return m_airports;
	}
	public void addAirport(AirportBean airport) {
		m_airports.add(airport);
	}
	public ArrayList getRoutes() {
		return m_routes;
	}
	public void addRoute(RouteBean route) {
		m_routes.add(route);
	}
	public boolean equals(Object obj) {
		if (obj instanceof TimeTableBean) {
			TimeTableBean compare = (TimeTableBean)obj;
			return Utils.equalLists(m_routes, compare.m_routes) &&
				Utils.equalLists(m_airports, compare.m_airports) &&
				Utils.equalLists(m_carriers, compare.m_carriers);
		} else {
			return false;
		}
	}
}

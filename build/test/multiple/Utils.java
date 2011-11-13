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
import java.util.List;

/**
 * Static utility class providing comparison methods.
 *
 * @author Dennis M. Sosnoski
 * @version 0.8
 */

public abstract class Utils
{
	public static boolean equalStrings(String a, String  b) {
		return (a == null && b == null) || a.equals(b);
	}
	
	public static boolean equalLists(List a, List  b) {
		return (a == null && b == null) || a.equals(b);
	}

	public static boolean equalAirports(AirportBean a, AirportBean b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}

	public static boolean equalCarriers(CarrierBean a, CarrierBean b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}

	public static boolean equalDates(Date a, Date b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}
	
	public static int timeToMinute(String time) {
		
		// find split between hour and minute
		int mark = time.indexOf(':');
		if (mark > 0) {
			
			// convert hour to adjusted range (0-11)
			int hour = Integer.parseInt(time.substring(0, mark));
			if (hour == 12) {
				hour = 0;
			}
			
			// find minute value
			String mins = time.substring(mark+1, time.length()-1);
			int minute = Integer.parseInt(mins);
			
			// check am/pm flag
			char last = time.charAt(time.length()-1);
			boolean pm = Character.toLowerCase(last) == 'p';
			
			// return minute number of flight time
			return ((pm ? 12 : 0) + hour)*60 + minute;
			
		} else {
			throw new IllegalArgumentException("Not a valid time: " + time);
		}
	}
	
	public static String minuteToTime(int minute) {
		
		// find hour in quirky am/pm form
		int hour = minute / 60;
		boolean pm = false;
		if (hour >= 12) {
			hour -= 12;
			pm = true;
		}
		if (hour == 0) {
			hour = 12;
		}
		
		// convert minute value to double-digit text
		minute %= 60;
		String mtext = Integer.toString(minute);
		if (mtext.length() == 1) {
			mtext = '0' + mtext;
		}
		
		// return complete time text
		return Integer.toString(hour) + ':' + mtext + (pm ? 'p' : 'a');
	}
}

/*
Copyright (c) 2003, Dennis M. Sosnoski
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

package simple;

import java.util.Iterator;

public class Customers6
{
	private int fillPosition;
	private Object[] customers;
    
    private void setCustomerCount(int count) {
        customers = new Object[count];
		fillPosition = 0;
    }
	
	private int getCustomerCount() {
		return fillPosition;
	}
	
	private void addCustomer(Object obj) {
		customers[fillPosition++] = obj;
	}
    
    private Iterator getCustomerIterator() {
        return new CustomerIterator();
    }
    
    private class CustomerIterator implements Iterator {
        
        int nextIndex;
        
        private CustomerIterator() {
            nextIndex = 0;
        }

        public boolean hasNext() {
            return nextIndex < fillPosition;
        }

        public Object next() {
            if (nextIndex < fillPosition) {
                return customers[nextIndex++];
            } else {
                return null;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("No remove support");
        }
    }
}

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

public class Customers7
{
    private DerivedCollection collection = new DerivedCollection();
    
    private void setCustomerCount(int count) {
        if (count > 0) {
            collection.setCustomerCount(count);
        } else {
            collection = null;
        }
    }
	
	private int getCustomerCount() {
        if (collection == null) {
            return 0;
        } else {
            return collection.getCustomerCount();
        }
	}
	
	private void wrappedAddCustomer(Object obj) {
		collection.addCustomer((CustomerInterface)obj);
	}
    
    private boolean wrappedHasCustomer() {
        return collection != null && collection.hasCustomer();
    }
    
    private Iterator wrappedGetCustomerIterator() {
        return new CustomerIterator(collection);
    }
    
    private static class CustomerCollection {
        
        private int fillPosition;
        private CustomerInterface[] customers;
        
        protected void setCustomerCount(int count) {
            customers = new Customer[count];
            fillPosition = 0;
        }
        
        protected int getCustomerCount() {
            return fillPosition;
        }
        
        protected void addCustomer(CustomerInterface obj) {
            customers[fillPosition++] = obj;
        }
        
        protected boolean hasCustomer() {
            return fillPosition > 0;
        }
        
        protected CustomerInterface getCustomer(int index) {
            return customers[index];
        }
        
        protected Iterator getCustomerIterator() {
            return new CustomerIterator(this);
        }
    }
    
    private static class DerivedCollection extends CustomerCollection {
    }
    
    private static class CustomerIterator implements Iterator {
        
        private final CustomerCollection collection;
        int nextIndex;
        
        private CustomerIterator(CustomerCollection coll) {
            collection = coll;
            nextIndex = 0;
        }

        public boolean hasNext() {
            return collection != null && nextIndex < collection.fillPosition;
        }

        public Object next() {
            if (nextIndex < collection.fillPosition) {
                return collection.getCustomer(nextIndex++);
            } else {
                return null;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("No remove support");
        }
    }
    
    public static CustomerInterface createCustomer() {
        return new Customer();
    }
    
    public interface CustomerInterface
    {
        public Name getName();
        public boolean isName();
        public void flagName(boolean flag);
        public String getStreet1();
        public String getCity();
        public String getState();
        public String getZip();
        public void setName(Name name);
        public void setStreet1(String street1);
        public void setCity(String city);
        public void setState(String state);
        public void setZip(String zip);
        public boolean isExtra();
        public void flagExtra(boolean flag);
        public boolean isSkipped();
        public void flagSkipped(boolean flag);
    }
    
    public interface ExtendedCustomerInterface extends CustomerInterface {}
	
	public static class Customer implements ExtendedCustomerInterface
	{
		private Name name;
        private boolean ifName;
		private String street1;
		private String city;
		private String state;
		private String zip;
        private boolean extra;
        private boolean skipped;
        public Name getName() { return name; }
        public boolean isName() { return ifName; }
        public void flagName(boolean flag) { ifName = flag; }
        public String getStreet1() { return street1; }
        public String getCity() { return city; }
        public String getState() { return state; }
        public String getZip() { return zip; }
        public void setName(Name name) { this.name = name; }
        public void setStreet1(String street1) { this.street1 = street1; }
        public void setCity(String city) { this.city = city; }
        public void setState(String state) { this.state = state; }
        public void setZip(String zip) { this.zip = zip; }
        public boolean isExtra() { return extra; }
        public void flagExtra(boolean flag) { extra = flag; }
        public boolean isSkipped() { return skipped; }
        public void flagSkipped(boolean flag) { skipped = flag; }
	}
}

package simple;

public class Customer9 {
    public int customerNumber;
    public String firstName;
    public String lastName;
    public Address9 shipAddress;
    public Address9 billAddress;
    public Address9 fakeAddress;
    public String phone;
    
    public Customer9() {
        // create fake address to make sure test method being called
        Address9 fake = new Address9();
        fake.street = "Nowhere";
    }
    
    public boolean isShipAddress() {
        return shipAddress != null;
    }
    
    public boolean isBillAddress() {
        return billAddress != null;
    }
    
    public boolean isFakeAddress() {
        return false;
    }
    
    public void setFakeAddress(Address9 address) {}
    
    public Address9 getFakeAddress() {
        throw new IllegalStateException("getFakeAddress() method called");
    }
}


package simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class MyClass6
{
    private final List ints = new ArrayList();
    private final List strings = new ArrayList();
    private byte a;
    private boolean b;
    private List lists;
    
    public static ArrayList createList() {
        throw new IllegalStateException
            ("Factory method should never be called");
    }
    
    protected void setInts(final List list) {
        if (list != ints) {
            throw new IllegalStateException
                ("Set method called with different list object");
        }
    }
    
    private boolean isPresent() {
        return ints.size() > 0;
    }
    
    private Iterator intsIterator() {
        return ints.iterator();
    }
    
    private void addInt(Object o) {
        ints.add((Integer)o);
    }
    
    private void addString(Object o) {
        strings.add((String)o);
    }
    
    private static class Lists
    {
        private List list1;
        private List list2;
        private List list3;
        private List list4;
    }
}

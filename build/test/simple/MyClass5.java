
package simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.jibx.runtime.IUnmarshallingContext;

public class MyClass5 {
	private ArrayList childs1;
    private ArrayList childs2;
    private ArrayList childs3;
    private Set altchilds2;
    private Set altchilds3;
	
	private void unmarshalDone() {
		for (int i = 0; i < childs1.size(); i++) {
			((MyClass5a)childs1.get(i)).verify();
		}
        if (childs2 != null) {
            for (int i = 0; i < childs2.size(); i++) {
                ((MyClass5a)childs2.get(i)).verify();
            }
        }
        if (childs3 != null) {
            for (int i = 0; i < childs3.size(); i++) {
                ((MyClass5a)childs3.get(i)).verify();
            }
        }
	}
	
	private static MyClass5a bFactory() {
		MyClass5b inst = new MyClass5b();
		inst.factory = true;
		return inst;
	}
	
	private static MyClass5a cFactory(Object obj) {
		if (!(obj instanceof ArrayList)) {
			throw new IllegalStateException("factory called with wrong object");
		}
		MyClass5c inst = new MyClass5c();
		inst.factory = true;
		return inst;
	}
	
	private static MyClass5a dFactory(IUnmarshallingContext ctx) {
		if (!(ctx.getStackObject(1) instanceof MyClass5)) {
			throw new IllegalStateException("wrong object in stack");
		}
		MyClass5d inst = new MyClass5d();
		inst.factory = true;
		return inst;
	}
    
    private static Set setFactory() {
        return new OrderedSet();
    }
    
    private static class OrderedSet implements Set
    {
        private final ArrayList list;
        
        private OrderedSet() {
            list = new ArrayList();
        }
        
        private OrderedSet(OrderedSet original) {
            list = new ArrayList(original.list);
        }

        public boolean add(Object arg0) {
            return list.add(arg0);
        }

        public boolean addAll(Collection arg0) {
            return list.addAll(arg0);
        }

        public void clear() {
            list.clear();
        }

        public Object clone() {
            return new OrderedSet(this);
        }

        public boolean contains(Object elem) {
            return list.contains(elem);
        }

        public boolean containsAll(Collection arg0) {
            return list.containsAll(arg0);
        }

        public boolean equals(Object o) {
            if (o instanceof OrderedSet) {
                return ((OrderedSet)o).list.equals(list);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return list.hashCode();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public Iterator iterator() {
            return list.iterator();
        }

        public boolean remove(Object o) {
            return list.remove(o);
        }

        public boolean removeAll(Collection arg0) {
            return list.removeAll(arg0);
        }

        public boolean retainAll(Collection arg0) {
            return list.retainAll(arg0);
        }

        public int size() {
            return list.size();
        }

        public Object[] toArray() {
            return list.toArray();
        }

        public Object[] toArray(Object[] arg0) {
            return list.toArray(arg0);
        }

        public String toString() {
            return list.toString();
        }
    }
}

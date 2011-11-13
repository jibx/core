
package simple;

import java.util.ArrayList;

class MyClass5c extends MyClass5a
{
	/*package*/ boolean factory;
	private boolean preset;
	private boolean postset;
	private int value;
	
	private void preset(Object obj) {
		if (!(obj instanceof ArrayList)) {
			throw new IllegalStateException("factory called with wrong object");
		}
		preset = true;
		value = 1;
	}
	
	private void postset(Object obj) {
		if (!(obj instanceof ArrayList)) {
			throw new IllegalStateException("factory called with wrong object");
		}
		postset = true;
		if (value == 2) {
			value = 3;
		}
	}
	
	private void preget(Object obj) {
		if (!(obj instanceof ArrayList)) {
			throw new IllegalStateException("factory called with wrong object");
		}
		value = 2;
	}
	
	protected void verify() {
		if (!factory || !preset || !postset) {
			throw new IllegalStateException
				("factory, pre-set, or post-set method not called");
		}
	}
}

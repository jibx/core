
package simple;

class MyClass5b extends MyClass5a
{
	/*package*/ boolean factory;
	private boolean preset;
	private boolean postset;
	private int value;
	
	private void preset() {
		preset = true;
		value = 1;
	}
	
	private void postset() {
		postset = true;
		if (value == 2) {
			value = 3;
		}
	}
	
	private void preget() {
		value = 2;
	}
	
	protected void verify() {
		if (!factory || !preset || !postset) {
			throw new IllegalStateException
				("factory, pre-set, or post-set method not called");
		}
	}
}

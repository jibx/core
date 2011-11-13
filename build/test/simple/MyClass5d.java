
package simple;

import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.ITrackSource;
import org.jibx.runtime.IUnmarshallingContext;

class MyClass5d extends MyClass5a
{
	/*package*/ boolean factory;
	private boolean preset;
	private boolean postset;
    private String text1;
    private String text2;
	
	private void preset(IUnmarshallingContext ctx) {
		if (!(ctx.getStackObject(1) instanceof MyClass5)) {
			throw new IllegalStateException("wrong object in stack: " +
                ctx.getStackObject(1).getClass().getName());
		}
		preset = true;
		value = 1;
	}
	
	private void postset(IUnmarshallingContext ctx) {
		if (!(ctx.getStackObject(1) instanceof MyClass5)) {
			throw new IllegalStateException("wrong object in stack: " +
                ctx.getStackObject(1).getClass().getName());
		}
		postset = true;
		if (value == 2) {
			value = 3;
		}
	}
	
	private void preget(IMarshallingContext ctx) {
		if (!(ctx.getStackObject(1) instanceof MyClass5)) {
			throw new IllegalStateException("wrong object in stack: " +
                ctx.getStackObject(1).getClass().getName());
		}
		value = 2;
	}
	
	protected void verify() {
		if (!factory || !preset || !postset) {
			throw new IllegalStateException
				("factory, pre-set, or post-set method not called");
		}
        ITrackSource track = (ITrackSource)this;
        System.out.println("Verified " + this.getClass().getName() +
            " from \"" + track.jibx_getDocumentName() + "\" (" +
            track.jibx_getLineNumber() + ":" + track.jibx_getColumnNumber() +
            ")");
	}
}

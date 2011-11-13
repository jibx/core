/*
Copyright (c) 2003-2005, Dennis M. Sosnoski
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

package org.jibx.binding.def;

import org.apache.bcel.generic.*;

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Collection binding definition. This handles one or more child components,
 * which may be ordered or unordered.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class NestedCollection extends NestedBase
{
    //
    // Method definitions used in code generation
    
    private static final String GROWARRAY_METHOD =
        "org.jibx.runtime.Utility.growArray";
    private static final String GROWARRAY_SIGNATURE =
        "(Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String RESIZEARRAY_METHOD =
        "org.jibx.runtime.Utility.resizeArray";
    private static final String RESIZEARRAY_SIGNATURE =
        "(ILjava/lang/Object;)Ljava/lang/Object;";
    private static final String CHECK_ISSTART_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.isStart";
    private static final String CHECK_ISSTART_SIGNATURE = "()Z";
    private static final String SKIP_ELEMENT_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.skipElement";
    private static final String SKIP_ELEMENT_SIGNATURE = "()V";

    //
    // Actual instance data.
    
    /** Fully qualified class name of values from collection. */
    private final String m_itemType;
    
    /** Strategy for generating code to load item from collection. */
    private final CollectionLoad m_loadStrategy;
    
    /** Strategy for generating code to store item to collection. */
    private final CollectionStore m_storeStrategy;
    
    /** Optional component flag. */
    private final boolean m_isOptional;
    
    /**
     * Constructor.
     *
     * @param parent containing binding definition context
     * @param objc current object context
     * @param ord ordered content flag
     * @param opt optional component flag
     * @param flex flexible element handling flag
     * @param type fully qualified class name of values from collection (may be
     * <code>null</code>, if child content present)
     * @param load collection load code generation strategy
     * @param store collection store code generation strategy
     */
    public NestedCollection(IContainer parent, IContextObj objc, boolean ord,
        boolean opt, boolean flex, String type, CollectionLoad load,
        CollectionStore store) {
        super(parent, objc, ord, flex, false);
        m_itemType = type;
        m_loadStrategy = load;
        m_storeStrategy = store;
        m_isOptional = opt;
    }
    
    /**
     * Get the collection item type.
     * 
     * @return item type
     */
    public String getItemType() {
        return m_itemType;
    }
    
    //
    // IComponent interface method definitions
    
    public void genNewInstance(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no instance creation");
    }

    public boolean hasAttribute() {
        return false;
    }

    public void genAttrPresentTest(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes present");
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes present");
    }

    public void genAttributeMarshal(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes present");
    }

    public boolean hasContent() {
        return m_contents.size() > 0;
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_contents.size() > 0) {
        
            // set up common handling and check for ordered or unordered content
            m_storeStrategy.genStoreInit(mb);
            BranchWrapper link = null;
            int count = m_contents.size();
            if (m_isOrdered) {
            
                // just generate unmarshal code for each component in order
                for (int i = 0; i < count; i++) {
                    
                    // start with branch target for loop and link from last type
                    if (link != null) {
                        mb.initStackState(link);
                    }
                    BranchTarget start = mb.appendTargetNOP();
                    if (link != null) {
                        link.setTarget(start, mb);
                    }
                    
                    // generate code to check if an element matching this
                    //  component type is present
                    IComponent child = (IComponent)m_contents.get(i);
                    child.genContentPresentTest(mb);
                    link = mb.appendIFEQ(this);
                    
                    // follow with code to unmarshal the component and store to
                    //  collection, ending with loop back to start of this
                    //  component
                    child.genContentUnmarshal(mb);
                    if (m_itemType != null && !ClassItem.isPrimitive(m_itemType)) {
                        mb.appendCreateCast(m_itemType);
                    }
                    m_storeStrategy.genStoreItem(mb);
                    mb.appendUnconditionalBranch(this).setTarget(start, mb);
                }
                
            } else {
            
                // generate unmarshal loop code that checks for each component,
                //  branching to the next component until one is found and
                //  exiting the loop only when no component is matched
                BranchTarget first = mb.appendTargetNOP();
                for (int i = 0; i < count; i++) {
                    if (link != null) {
                        mb.targetNext(link);
                    }
                    IComponent child = (IComponent)m_contents.get(i);
                    child.genContentPresentTest(mb);
                    link = mb.appendIFEQ(this);
                    child.genContentUnmarshal(mb);
                    if (m_itemType != null && !ClassItem.isPrimitive(m_itemType)) {
                        mb.appendCreateCast(m_itemType);
                    }
                    m_storeStrategy.genStoreItem(mb);
                    mb.appendUnconditionalBranch(this).setTarget(first, mb);
                }
                
                // handle fall through condition depending on flexible flag
                if (m_isFlexible) {
                    
                    // exit loop if not positioned at element start
                    mb.targetNext(link);
                    mb.loadContext();
                    mb.appendCallVirtual(CHECK_ISSTART_NAME,
                        CHECK_ISSTART_SIGNATURE);
                    link = mb.appendIFEQ(this);
                    
                    // ignore unknown element and loop back to start
                    mb.loadContext();
                    mb.appendCallVirtual(SKIP_ELEMENT_NAME,
                        SKIP_ELEMENT_SIGNATURE);
                    mb.appendUnconditionalBranch(this).setTarget(first, mb);
                    
                }
            }
            
            // patch final test failure branch to fall through loop
            mb.targetNext(link);
            m_storeStrategy.genStoreDone(mb);
            
        } else {
            throw new IllegalStateException
                ("Internal error - no content present");
        }
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_contents.size() > 0) {
            
            // set up common handling of unknown item and collection empty
            BranchWrapper[] ifempties;
            BranchWrapper link = null;
            m_loadStrategy.genLoadInit(mb);
        
            // check for ordered or unordered content
            int count = m_contents.size();
            if (m_isOrdered) {
            
                // generate marshal code for each component type in order, with
                //  an exception generated if the end of the possible component
                //  list is reached with anything left in the collection
                ifempties = new BranchWrapper[count];
                for (int i = 0; i < count; i++) {
                    
                    // start generated code with loading next value from
                    //  collection
                    if (link != null) {
                        mb.initStackState(link, 1);
                    }
                    BranchTarget start = mb.appendTargetNOP();
                    ifempties[i] = m_loadStrategy.genLoadItem(mb);
                    mb.targetNext(link);
                    
                    // if multiple types are included in content, append code to
                    //  check if item type matches this component
                    IComponent child = (IComponent)m_contents.get(i);
                    String type = child.getType();
                    if (count > 1) {
                        mb.appendDUP();
                        mb.appendInstanceOf(type);
                        link = mb.appendIFEQ(this);
                    }
                    if ((!"java.lang.Object".equals(type) &&
                        !ClassItem.isPrimitive(type))) {
                        mb.appendCreateCast(type);
                    }
                    
                    // finish with code to marshal the component, looping back
                    //  to start of block for more of same type
                    child.genContentMarshal(mb);
                    mb.appendUnconditionalBranch(this).setTarget(start, mb);
                }
                
            } else {
            
                // generate marshal loop code that loads an item from the
                //  collection and then checks to see if it matches a component
                //  type, branching to the next component until a match is found
                //  (or generating an exception on no match)
                BranchTarget start = mb.appendTargetNOP();
                ifempties = new BranchWrapper[1];
                ifempties[0] = m_loadStrategy.genLoadItem(mb);
                for (int i = 0; i < count; i++) {
                    
                    // start by setting target for branch from last component
                    mb.targetNext(link);
                    
                    // if multiple types are included in content, append code to
                    //  check if item type matches this component
                    IComponent child = (IComponent)m_contents.get(i);
                    String type = child.getType();
                    if (count > 1 || (!"java.lang.Object".equals(type) &&
                        !ClassItem.isPrimitive(type))) {
                        mb.appendDUP();
                        mb.appendInstanceOf(type);
                        link = mb.appendIFEQ(this);
                        mb.appendCreateCast(type);
                    }
                    
                    // finish with code to marshal the component, branching back
                    //  to start of loop
                    child.genContentMarshal(mb);
                    mb.appendUnconditionalBranch(this).setTarget(start, mb);
                }
            
            }
        
            // patch final test failure branch to generate an exception
            if (link != null) {
            
                // instruction sequence for exception is create new exception
                //  object, build message in StringBuffer using type of item
                //  from stack, convert StringBuffer to String, invoke the
                //  exeception constructor with the String, and finally throw
                //  the exception
                mb.targetNext(link);
                mb.appendCreateNew("java.lang.StringBuffer");
                mb.appendDUP();
                mb.appendLoadConstant("Collection item of type ");
                mb.appendCallInit("java.lang.StringBuffer",
                    "(Ljava/lang/String;)V");
                mb.appendSWAP();
                mb.appendDUP();
                BranchWrapper ifnull = mb.appendIFNULL(this);
                mb.appendCallVirtual("java.lang.Object.getClass",
                    "()Ljava/lang/Class;");
                mb.appendCallVirtual("java.lang.Class.getName",
                    "()Ljava/lang/String;");
                BranchWrapper toend = mb.appendUnconditionalBranch(this);
                mb.targetNext(ifnull);
                mb.appendPOP();
                mb.appendLoadConstant("NULL");
                mb.targetNext(toend);
                mb.appendCallVirtual("java.lang.StringBuffer.append", 
                    "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
                mb.appendLoadConstant(" has no binding defined");
                mb.appendCallVirtual("java.lang.StringBuffer.append", 
                    "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
                mb.appendCallVirtual("java.lang.StringBuffer.toString",
                    "()Ljava/lang/String;");
                mb.appendCreateNew(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
                mb.appendDUP_X1();
                mb.appendSWAP();
                mb.appendCallInit(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS,
                    MethodBuilder.EXCEPTION_CONSTRUCTOR_SIGNATURE1);
                mb.appendThrow();
            }
            
            // finish by setting target for collection empty case(s)
            m_loadStrategy.genLoadDone(mb);
            mb.targetNext(ifempties);
            
        } else {
            throw new IllegalStateException
                ("Internal error - no content present");
        }
    }
    
    public boolean hasId() {
        return false;
    }
    
    public void genLoadId(ContextMethodBuilder mb) throws JiBXException {
        throw new IllegalStateException("No ID child");
    }
    
    public NameDefinition getWrapperName() {
        return null;
    }
    
    public boolean isOptional() {
        return m_isOptional;
    }

    public void setLinkages() throws JiBXException {
        for (int i = 0; i < m_contents.size(); i++) {
            ((IComponent)m_contents.get(i)).setLinkages();
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("collection " +
            (m_isOrdered ? "ordered" : "unordered"));
        if (m_itemType != null) {
            System.out.print(" (" + m_itemType + ")");
        }
        if (isFlexible()) {
            System.out.print(", flexible");
        }
        System.out.println();
        for (int i = 0; i < m_contents.size(); i++) {
            IComponent comp = (IComponent)m_contents.get(i);
            comp.print(depth+1);
        }
    }
    
    /**
     * Base class for collection item load strategy. The implementation class
     * must handle the appropriate form of code generation for the type of
     * collection being used.
     */
    /*package*/ static abstract class CollectionBase
    {
        /** Double word value flag. */
        private final boolean m_isDoubleWord;
        
        /**
         * Constructor.
         *
         * @param doubword double word value flag
         */
        protected CollectionBase(boolean doubword) {
            m_isDoubleWord = doubword;
        }
        
        /**
         * Append the appropriate instruction to swap the top of the stack
         * (which must be a single-word value) with an item value (which may
         * be one or two words, as configured for this collection).
         * 
         * @param mb method
         */
        protected void appendSWAP(MethodBuilder mb) {
            if (m_isDoubleWord) {
                mb.appendSWAP1For2();
            } else {
                mb.appendSWAP();
            }
        }
        
        /**
         * Append the appropriate instruction to pop the item value (which may
         * be one or two words, as configured for this collection) from the top
         * of the stack.
         * 
         * @param mb method
         */
        protected void appendPOP(MethodBuilder mb) {
            if (m_isDoubleWord) {
                mb.appendPOP2();
            } else {
                mb.appendPOP();
            }
        }
    }
    
    /**
     * Base class for collection item load strategy. The implementation class
     * must handle the appropriate form of code generation for the type of
     * collection being used.
     */
    /*package*/ static abstract class CollectionLoad extends CollectionBase
    {
        /**
         * Constructor.
         *
         * @param doubword double word value flag
         */
        protected CollectionLoad(boolean doubword) {
            super(doubword);
        }
        
        /**
         * Generate code to initialize collection for loading items. This
         * generates the necessary code for handling the initialization. It
         * must be called before attempting to call the {@link #genLoadItem}
         * method. The base class implementation does nothing.
         *
         * @param mb method builder
         * @throws JiBXException if error in configuration
         */
        protected void genLoadInit(ContextMethodBuilder mb)
            throws JiBXException {}
        
        /**
         * Generate code to load next item from collection. This generates the
         * necessary code for handling the load operation, leaving the item on
         * the stack. The {@link #genLoadInit} method must be called before
         * calling this method, and the {@link #genLoadDone} method must be
         * called after the last call to this method. This method must be
         * overridden by each subclass.
         *
         * @param mb method builder
         * @return branch wrapper for case of done with collection
         * @throws JiBXException if error in configuration
         */
        protected abstract BranchWrapper genLoadItem(ContextMethodBuilder mb)
            throws JiBXException;
        
        /**
         * Generate code to clean up after loading items from collection. This
         * generates the necessary code for handling the clean up. It must be
         * called after the last call to {@link #genLoadItem}. The base class
         * implementation does nothing.
         *
         * @param mb method builder
         * @throws JiBXException if error in configuration
         */
        protected void genLoadDone(ContextMethodBuilder mb)
            throws JiBXException {}
    }
    
    /**
     * Base class for collection item store strategy. The implementation class
     * must handle the appropriate form of code generation for the type of
     * collection being used.
     */
    /*package*/ static abstract class CollectionStore extends CollectionBase
    {
        /**
         * Constructor.
         *
         * @param doubword double word value flag
         */
        protected CollectionStore(boolean doubword) {
            super(doubword);
        }
        
        /**
         * Generate code to initialize collection for storing items. This
         * generates the necessary code for handling the initialization,
         * including creating the collection object if appropriate. It must be
         * called before attempting to call the {@link #genStoreItem} method.
         * The base class implementation does nothing.
         *
         * @param mb method builder
         * @throws JiBXException if error in configuration
         */
        protected void genStoreInit(ContextMethodBuilder mb)
            throws JiBXException {}
        
        /**
         * Generate code to store next item to collection. This generates the
         * necessary code for handling the store operation, removing the item
         * from the stack. The {@link #genStoreInit} method must be called
         * before calling this method, and the {@link #genStoreDone} method must
         * be called after the last call to this method. This method must be
         * overridden by each subclass.
         *
         * @param mb method builder
         * @throws JiBXException if error in configuration
         */
        protected abstract void genStoreItem(ContextMethodBuilder mb)
            throws JiBXException;
        
        /**
         * Generate code to clean up after storing items to collection. This
         * generates the necessary code for handling the clean up. It must be
         * called after the last call to {@link #genStoreItem}. The base class
         * implementation does nothing.
         *
         * @param mb method builder
         * @throws JiBXException if error in configuration
         */
        protected void genStoreDone(ContextMethodBuilder mb)
            throws JiBXException {}
    }
    
    /**
     * Collection item load strategy for collection with items accessed by
     * index number.
     */
    /*package*/ static class IndexedLoad extends CollectionLoad
    {
        /** Method used to get count of items in collection. */
        private final ClassItem m_sizeMethod;
        
        /** Method used to get items by index from collection. */
        private final ClassItem m_getMethod;
        
        /**
         * Constructor.
         *
         * @param size method used to get count of items in collection
         * @param doubword double word value flag
         * @param get method used to retrieve items by index from collection
         */
        /*package*/ IndexedLoad(ClassItem size, boolean doubword,
            ClassItem get) {
            super(doubword);
            m_sizeMethod = size;
            m_getMethod = get;
        }
        
        protected void genLoadInit(ContextMethodBuilder mb)
            throws JiBXException {
            
            // create index local with appended code to set initial value
            mb.appendLoadConstant(-1);
            mb.defineSlot(m_getMethod, Type.INT);
            
            // create size local with appended code to set initial value from
            //  collection method call
            if (!m_sizeMethod.isStatic()) {
                mb.loadObject();
            }
            mb.appendCall(m_sizeMethod);
            mb.defineSlot(m_sizeMethod, Type.INT);
        }

        protected BranchWrapper genLoadItem(ContextMethodBuilder mb)
            throws JiBXException {
            
            // start by getting local variable slots for the index and size
            int islot = mb.getSlot(m_getMethod);
            int sslot = mb.getSlot(m_sizeMethod);
            
            // append code to first increment index, then check for end of
            //  collection reached
            mb.appendIncrementLocal(1, islot);
            mb.appendLoadLocal(islot);
            mb.appendLoadLocal(sslot);
            mb.appendISUB();
            BranchWrapper ifempty = mb.appendIFGE(this);
            
            // finish by calling collection method to load item at current index
            //  position
            if (!m_getMethod.isStatic()) {
                mb.loadObject();
            }
            mb.appendLoadLocal(islot);
            mb.appendCall(m_getMethod);
            return ifempty;
        }

        protected void genLoadDone(ContextMethodBuilder mb)
            throws JiBXException {
            mb.freeSlot(m_getMethod);
            mb.freeSlot(m_sizeMethod);
        }
    }
    
    /**
     * Collection item store strategy for collection with items set by
     * index number.
     */
    /*package*/ static class IndexedStore extends CollectionStore
    {
        /** Method used to set items by index in collection. */
        private final ClassItem m_setMethod;
        
        /** Flag for method returns result. */
        private final boolean m_isReturned;
        
        /**
         * Constructor.
         *
         * @param set method used to store items by index in collection
         * @param doubword double word value flag
         * @param ret value returned by add flag
         */
        /*package*/ IndexedStore(ClassItem set, boolean doubword, boolean ret) {
            super(doubword);
            m_setMethod = set;
            m_isReturned = ret;
        }
        
        protected void genStoreInit(ContextMethodBuilder mb)
            throws JiBXException {
            
            // create index local with appended code to set initial value
            mb.appendLoadConstant(-1);
            mb.defineSlot(m_setMethod, Type.INT);
        }

        protected void genStoreItem(ContextMethodBuilder mb)
            throws JiBXException {
            
            // start by getting local variable slot for the index
            int islot = mb.getSlot(m_setMethod);
            
            // append code to first load object and swap with item, then
            //  increment index and swap copy with item, and finally call
            //  collection method to store item at new index position
            if (!m_setMethod.isStatic()) {
                mb.loadObject();
                appendSWAP(mb);
            }
            mb.appendIncrementLocal(1, islot);
            mb.appendLoadLocal(islot);
            appendSWAP(mb);
            mb.appendCall(m_setMethod);
            if (m_isReturned) {
                appendPOP(mb);
            }
        }

        protected void genStoreDone(ContextMethodBuilder mb)
            throws JiBXException {
            mb.freeSlot(m_setMethod);
        }
    }
    
    /**
     * Collection item load strategy for collection with items accessed by
     * iterator or enumeration.
     */
    /*package*/ static class IteratorLoad extends CollectionLoad
    {
        /** Method used to get iterator for collection. */
        private final ClassItem m_iterMethod;
        
        /** Fully qualified method name to test if more in iteration. */
        private final String m_moreName;
        
        /** Fully qualified method name to get next item in iteration. */
        private final String m_nextName;
        
        /**
         * Constructor.
         *
         * @param iter method to get iterator or enumerator from collection
         * @param doubword double word value flag
         * @param more fully qualified method name to test if more in iteration
         * @param next fully qualified method name to get next item in iteration
         */
        /*package*/ IteratorLoad(ClassItem iter, boolean doubword, String more,
            String next) {
            super(doubword);
            m_iterMethod = iter;
            m_moreName = more;
            m_nextName = next;
        }
        
        protected void genLoadInit(ContextMethodBuilder mb)
            throws JiBXException {
            
            // create iterator local with appended code to set initial value
            if (!m_iterMethod.isStatic()) {
                mb.loadObject();
            }
            mb.appendCall(m_iterMethod);
            mb.defineSlot(m_iterMethod, Type.getType("Ljava/util/Iterator;"));
        }

        protected BranchWrapper genLoadItem(ContextMethodBuilder mb)
            throws JiBXException {
            
            // start with code to load and test iterator
            int islot = mb.getSlot(m_iterMethod);
            mb.appendLoadLocal(islot);
            mb.appendCallInterface(m_moreName, "()Z");
            BranchWrapper ifempty = mb.appendIFEQ(this);
            
            // append code to get next item from iterator
            mb.appendLoadLocal(islot);
            mb.appendCallInterface(m_nextName, "()Ljava/lang/Object;");
            return ifempty;
        }

        protected void genLoadDone(ContextMethodBuilder mb)
            throws JiBXException {
            mb.freeSlot(m_iterMethod);
        }
    }
    
    /**
     * Collection item store strategy for collection with add method.
     */
    /*package*/ static class AddStore extends CollectionStore
    {
        /** Method used to add item to collection. */
        private final ClassItem m_addMethod;
        
        /** Flag for method returns result. */
        private final boolean m_isReturned;
        
        /**
         * Constructor.
         *
         * @param add method used to add item to collection
         * @param doubword double word value flag
         * @param ret value returned by add flag
         */
        /*package*/ AddStore(ClassItem add, boolean doubword, boolean ret) {
            super(doubword);
            m_addMethod = add;
            m_isReturned = ret;
        }

        protected void genStoreItem(ContextMethodBuilder mb)
            throws JiBXException {
            
            // append code to call collection method to add the item
            if (!m_addMethod.isStatic()) {
                mb.loadObject();
                appendSWAP(mb);
            }
            mb.appendCall(m_addMethod);
            if (m_isReturned) {
                appendPOP(mb);
            }
        }
    }
    
    /**
     * Collection item load strategy for array.
     */
    /*package*/ static class ArrayLoad extends CollectionLoad
    {
        /** Array item type. */
        private final String m_itemType;
        
        /** Handle for referencing loop counter local variable. */
        private Object m_slotHandle = new Object();
        
        /**
         * Constructor.
         *
         * @param itype array item type
         * @param doubword double word value flag
         */
        /*package*/ ArrayLoad(String itype, boolean doubword) {
            super(doubword);
            m_itemType = itype;
        }
        
        protected void genLoadInit(ContextMethodBuilder mb)
            throws JiBXException {
            
            // create index local with initial value -1
            mb.appendLoadConstant(-1);
            mb.defineSlot(m_slotHandle, Type.INT);
        }

        protected BranchWrapper genLoadItem(ContextMethodBuilder mb)
            throws JiBXException {
            
            // start by getting local variable slots for the index
            int islot = mb.getSlot(m_slotHandle);
            
            // append code to first increment index, then check for end of
            //  collection reached
            mb.appendIncrementLocal(1, islot);
            mb.appendLoadLocal(islot);
            mb.loadObject();
            mb.appendARRAYLENGTH();
            mb.appendISUB();
            BranchWrapper ifempty = mb.appendIFGE(this);
            
            // finish by loading array item at current index position
            mb.loadObject();
            mb.appendLoadLocal(islot);
            mb.appendALOAD(m_itemType);
            return ifempty;
        }

        protected void genLoadDone(ContextMethodBuilder mb)
            throws JiBXException {
            mb.freeSlot(m_slotHandle);
        }
    }
    
    /**
     * Collection item store strategy for array.
     */
    /*package*/ static class ArrayStore extends CollectionStore
    {
        /** Array item type. */
        private final String m_itemType;
        
        /**
         * Constructor.
         *
         * @param itype array item type
         * @param doubword double word value flag
         */
        /*package*/ ArrayStore(String itype, boolean doubword) {
            super(doubword);
            m_itemType = itype;
        }
        
        protected void genStoreInit(ContextMethodBuilder mb)
            throws JiBXException {
            
            // create index local with initial value -1
            mb.appendLoadConstant(-1);
            mb.defineSlot(m_itemType, Type.INT);
        }

        protected void genStoreItem(ContextMethodBuilder mb)
            throws JiBXException {
            
            // start by getting local variable slot for the index
            int islot = mb.getSlot(m_itemType);
            
            // append code to first increment index and check array size
            mb.appendIncrementLocal(1, islot);
            mb.appendLoadLocal(islot);
            mb.loadObject();
            mb.appendARRAYLENGTH();
            mb.appendISUB();
            BranchWrapper ifnotfull = mb.appendIFLT(this);
            
            // grow the array size to make room for more values
            mb.loadObject();
            mb.appendCallStatic(GROWARRAY_METHOD, GROWARRAY_SIGNATURE);
            mb.storeObject();
            
            // swap the array reference with the item, swap index with item, and
            //  finally store item at new index position
            mb.targetNext(ifnotfull);
            mb.loadObject();
            appendSWAP(mb);
            mb.appendLoadLocal(islot);
            appendSWAP(mb);
            if (!ClassItem.isPrimitive(m_itemType)) {
                mb.appendCreateCast(m_itemType);
            }
            mb.appendASTORE(m_itemType);
        }

        protected void genStoreDone(ContextMethodBuilder mb)
            throws JiBXException {
            
            // resize the array to match actual item count
            int islot = mb.getSlot(m_itemType);
            mb.appendIncrementLocal(1, islot);
            mb.appendLoadLocal(islot);
            mb.loadObject();
            mb.appendCallStatic(RESIZEARRAY_METHOD, RESIZEARRAY_SIGNATURE);
            mb.storeObject();
            mb.freeSlot(m_itemType);
        }
    }
}
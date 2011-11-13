package org.jibx.v2;

import org.jibx.runtime.JiBXException;

/**
 * Interface implemented by all classes corresponding to global schema definitions and &lt;complexType> definitions
 * (global or nested). This includes &lt;attributeGroup> and &lt;group> definitions, as well as global &lt;attribute>
 * and &lt;element> definitions. Besides the actual interface methods, classes which correspond to a complexType must
 * define an unmarshalling method <code>public static ClassName _check_substituted(XmlReader, ClassName)</code> to check
 * for type substitution, those which correspond to an element or group must define a method <code>public static boolean
 * _is_present()</code> to check if an optional instance of the element or group is present, and classes which
 * correspond to anything <i>other</i> than a complexType must define a <code>public static ClassName
 * _check_instance(ClassName)</code> method to return an instance of the class (where the supplied instance may be
 * <code>null</code>, or an instance of a subclass).
 */
public interface MappedStructure
{
    /**
     * Marshal the structure representation. In the case of an &lt;attribute>, &lt;attributeGroup>, or &lt;complexType>
     * structure the writer must be positioned on the element start tag at the time of this call.
     *
     * @param wrtr
     * @throws JiBXException
     */
    void _marshal(XmlWriter wrtr) throws JiBXException;
    
    /**
     * Unmarshal the structure representation. In the case of an &lt;attribute>, &lt;attributeGroup>, or
     * &lt;complexType> structure the reader must be positioned on the element start tag at the time of this call.
     *
     * @param rdr
     * @throws JiBXException
     */
    void _unmarshal(XmlReader rdr) throws JiBXException;
}
package org.jibx.v2;

import org.jibx.runtime.QName;

/**
 * Interface implemented by all classes corresponding to global &lt;complexType> definitions when type substitution is
 * enabled. Besides the actual interface methods, classes also define a
 * <code>public static ClassName _check_substituted(XmlReader, ClassName)</code> method, which returns an
 * unmarshalled instance of the appropriate type, after taking type substitution into account.
 */
public interface MappedType extends MappedStructure
{
    /**
     * Get the qualified name of this type.
     *
     * @return qualified name
     */
    QName _get_type_qname();
}
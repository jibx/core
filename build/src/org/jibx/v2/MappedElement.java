package org.jibx.v2;

import org.jibx.runtime.QName;

/**
 * Interface implemented by all classes corresponding to schema global &lt;element> definitions.
 */
public interface MappedElement extends MappedStructure
{
    /**
     * Get the qualified name of the element used for this instance.
     *
     * @return qualified name
     */
    QName _get_element_qname();
}
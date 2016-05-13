package org.rust.lang.core.psi

interface RustInnerAttributeOwner : RustCompositeElement {
    /**
     * Outer attributes are always children of the owning node.
     * In contrast, inner attributes can be either direct
     * children or grandchildren.
     */
    val innerAttrList: List<RustInnerAttr>
}

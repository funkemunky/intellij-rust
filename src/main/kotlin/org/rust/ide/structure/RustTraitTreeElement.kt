package org.rust.ide.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import org.rust.lang.core.psi.RustTraitItemElement

class RustTraitTreeElement(element: RustTraitItemElement) : PsiTreeElementBase<RustTraitItemElement>(element) {

    override fun getPresentableText(): String {
        var text = element?.identifier?.text ?: return "<unknown>"

        val generics = element?.typeParameterList?.text
        if (generics != null)
            text += generics

        val typeBounds = element?.typeParamBounds?.polyboundList?.map { it.text }?.joinToString(" + ")
        if (typeBounds != null)
            text += ": $typeBounds"

        return text
    }

    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val trait = element ?: return emptyList()
        return listOf(
            trait.functionList.map(::RustFunctionTreeElement),
            trait.constantList.map(::RustConstantTreeElement)
        ).flatten().sortedBy { it.element?.textOffset }
    }
}

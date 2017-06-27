/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.lang.core.psi.ext

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsMetaItem
import org.rust.lang.core.resolve.ref.RsMetaItemReferenceImpl
import org.rust.lang.core.resolve.ref.RsReference

abstract class RsMetaItemImplMixin(node: ASTNode) : RsCompositeElementImpl(node), RsMetaItem {

    override val referenceNameElement: PsiElement get() = identifier

    override val referenceName: String get() = referenceNameElement.text

    override fun getReference(): RsReference = RsMetaItemReferenceImpl(this)
}

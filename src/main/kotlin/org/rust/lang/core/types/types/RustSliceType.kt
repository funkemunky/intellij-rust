package org.rust.lang.core.types.types

import com.intellij.openapi.project.Project
import org.rust.lang.core.types.Ty

data class RustSliceType(val elementType: Ty) : RustPrimitiveType {
    override fun toString() = "[$elementType]"

    override fun canUnifyWith(other: Ty, project: Project): Boolean {
        return other is RustSliceType && elementType.canUnifyWith(other.elementType, project)
    }

    override fun substitute(map: Map<RustTypeParameterType, Ty>): Ty {
        return RustSliceType(elementType.substitute(map))
    }
}

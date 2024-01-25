/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.refactoring

import org.intellij.lang.annotations.Language
import org.rust.RsTestBase
import org.rust.launchAnAction

class ConvertToTupleRefactoringTest : RsTestBase() {
    fun `test simple`() = doAvailableTest("""
        struct Test{/*caret*/
            pub a:usize,
            b:i32
        }
    """, """
        struct Test(pub usize, i32);
    """)

    fun `test empty`() = doAvailableTest("""
        struct Test{/*caret*/}
    """, """
        struct Test();
    """)

    fun `test simple enum`() = doAvailableTest("""
        enum Test{
            A{ /*caret*/a:usize,
                b:i32 },
            B
        }
        fn main(){
            let Test::A{a,b} = Test::A {a:0,b:0};
        }
    """, """
        enum Test{
            A(usize, i32),
            B
        }
        fn main(){
            let Test::A(a, b) = Test::A(0, 0);
        }
    """)

    fun `test convert struct literal`() = doAvailableTest("""
        struct Test{/*caret*/
            pub a:usize,
            b:i32
        }
        fn main (){
            let (a, b) = (0, 0);
            let x = Test{a: 0,b: 0};
            let x = Test{b: 1,a: 0};
            let x = Test{a, b};
            let a = 0;
            let x = Test{a, ..x };
        }
    """, """
        struct Test(pub usize, i32);

        fn main (){
            let (a, b) = (0, 0);
            let x = Test(0, 0);
            let x = Test(0, 1);
            let x = Test(a, b);
            let a = 0;
            let x = Test { 0: a, ..x };
        }
    """)

    fun `test convert field access`() = doAvailableTest("""
        struct Test{/*caret*/
            pub a:usize,
            b:i32
        }
        fn main (){
            let var = Test{a: 0, b: 0};
            let x = var.a;
            let x = var.b;
        }
    """, """
        struct Test(pub usize, i32);

        fn main (){
            let var = Test(0, 0);
            let x = var.0;
            let x = var.1;
        }
    """)

    fun `test convert destructuring`() = doAvailableTest("""
        struct Test{/*caret*/
            pub a:usize,
            b:i32
        }
        fn main (){
            let Test{a, b} = Test{a: 0, b: 0};
            let Test{ref a, b:mut var} = Test{a: 0, b: 0};
            let Test{a:var1, b:var2} = Test{a: 0, b: 0};
            let Test{a, ..} = Test{a: 0, b: 0};
            let Test{b:var, ..} = Test{a: 0, b: 0};
        }
    """, """
        struct Test(pub usize, i32);

        fn main (){
            let Test(a, b) = Test(0, 0);
            let Test(ref a, mut var) = Test(0, 0);
            let Test(var1, var2) = Test(0, 0);
            let Test(a, _) = Test(0, 0);
            let Test(_, var) = Test(0, 0);
        }
    """)

    fun `test convert function call`() = doAvailableTest("""
        struct S {
            /*caret*/a: u32
        }
        impl S {
            fn new(v: u32) -> S {
                S { a: v }
            }
        }

        fn main() {
            let s = S::new(0);
        }
    """, """
        struct S(u32);

        impl S {
            fn new(v: u32) -> S {
                S(v)
            }
        }

        fn main() {
            let s = S::new(0);
        }
    """)

    fun `test where clause`() = doAvailableTest("""
        trait Trait {}
        struct Test<T> where T: Trait {/*caret*/
            a: T,
        }
    """, """
        trait Trait {}
        struct Test<T>(T) where T: Trait;
    """)

    fun `test where clause (multiline)`() = doAvailableTest("""
        trait Trait {}
        struct Test<T1, T2>
            where
                T1: Trait,
                T2: Trait,
        {/*caret*/
            a: T1,
            b: T2,
        }
    """, """
        trait Trait {}
        struct Test<T1, T2>(T1, T2)
            where
                T1: Trait,
                T2: Trait;
    """)

    fun `test replace Self in impls`() = doAvailableTest("""
        struct Test/*caret*/ { a: i32, b: i32 }
        impl Test {
            fn new() -> Self {
                let b = 1;
                Self { a: 0, b }
            }
        }
    """, """
        struct Test(i32, i32);

        impl Test {
            fn new() -> Self {
                let b = 1;
                Self(0, b)
            }
        }
    """)

    private fun doAvailableTest(@Language("Rust") before: String, @Language("Rust") after: String) {
        InlineFile(before.trimIndent()).withCaret()
        myFixture.launchAnAction("Rust.RsConvertToTuple")
        myFixture.checkResult(replaceCaretMarker(after.trimIndent()))
    }
}

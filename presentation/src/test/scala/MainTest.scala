package presentation

import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary

class MainSpec
    extends org.scalatest.freespec.AnyFreeSpec
    with org.scalatest.matchers.should.Matchers
    with org.scalatestplus.scalacheck.ScalaCheckPropertyChecks {
  val COMP = (a: Int, b: Int) => (a <= b)

  "uncurry" - {

    "Should not change value of the function for random functions" in {
      forAll("f") { (f: Int => Int => Int) =>
        forAll("n", "m") { (n: Int, m: Int) =>
          f(n)(m) shouldBe Exercises.uncurry(f)(n, m)
        }
      }
    }

    "The type signature must not have been changed" in {
      "def f[A,B,C] = (Exercises.uncurry : (A => B => C) => (A,B) => C)" should compile
    }

  }
}

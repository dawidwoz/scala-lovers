package exercises

import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary

class ExercisesSpec
    extends org.scalatest.freespec.AnyFreeSpec
    with org.scalatest.matchers.should.Matchers
    with org.scalatestplus.scalacheck.ScalaCheckPropertyChecks {
  val COMP = (a: Int, b: Int) => (a <= b)
  val easyCOMP = (a: Int, b: Int) => true
  val invCOMP = (a: Int, b: Int) => (a >= b)

  val genIntArray: Gen[Array[Int]] = Gen
    .choose(0, 10000)
    .flatMap { n => Gen.listOfN[Int](n, arbitrary[Int]) }
    .map { _.toArray }
  "square" - {

    "should not throw exceptions" in {
      forAll("n") { (n: Int) =>
        noException should be thrownBy Exercises.square(n)
      }
    }

    // does not test for overflow, but this is not the point here
    "behaves like n*n" in {
      forAll("n") { (n: Int) => Exercises.square(n) should be(n * n) }
    }

  }

  "compose" - {

    "Should be associative: (compose(compose (f,g),h) == compose (f, compose(g,h))" in {
      withClue(
        "Poor diagnostic info. Unfortunately, hard to do for generated function values:\n"
      ) {
        forAll("f", "g", "h") { (f: Int => Int, g: Int => Int, h: Int => Int) =>
          forAll("n") { (n: Int) =>
            Exercises.compose(Exercises.compose(f, g), h)(n) shouldBe
              Exercises.compose(f, Exercises.compose(g, h))(n)
          }
        }
      }
    }

    "Left composition with identity does not change the function" in {
      withClue(
        "Poor diagnostic info. Unfortunately, hard to do for generated function values:\n"
      ) {
        forAll("f") { (f: String => String) =>
          forAll("n") { (s: String) =>
            Exercises.compose(_: String => String, f)(s) shouldBe f(s)
          }
        }
      }
    }

    "Right composition with identity does not change the function" in {
      withClue(
        "Poor diagnostic info. Unfortunately, hard to do for generated function values:\n"
      ) {
        forAll("f") { (f: Int => String) =>
          forAll("n") { (n: Int) =>
            Exercises.compose(f, _: Int => Int)(n) shouldBe f(n)
          }
        }
      }
    }
  }

  "curry" - {

    "Should not throw exception" in { Exercises.curry(Exercises.isSorted _) }

    "Should not change value of the function for random functions" in {
      forAll("f") { (f: (Int, Int) => Int) =>
        forAll("n", "m") { (n: Int, m: Int) =>
          f(n, m) shouldBe Exercises.curry(f)(n)(m)
        }
      }
    }

    "Should not change the value of isSorted" in {
      forAll((genIntArray, "as")) { (as: Array[Int]) =>
        Exercises.isSorted[Int](as.sorted, COMP) shouldBe
          Exercises.curry(Exercises.isSorted[Int] _)(as.sorted)(COMP)
      }
    }

    "The type signature must not have been changed" in {
      "def f[A,B,C] = (Exercises.curry : ((A,B) => C) => A => B => C)" should compile
    }

  }

  "isSorted" - {

    "Array(1,2,3,4,5,6)" in {
      Exercises.isSorted(Array(1, 2, 3, 4, 5, 6), COMP) shouldBe true
    }

    "Array(6,2,3,4,5,6)" in {
      Exercises.isSorted(Array(6, 2, 3, 4, 5, 6), COMP) shouldBe false
    }

    "Array(1,2,3,4,5,1)" in {
      Exercises.isSorted(Array(1, 2, 3, 4, 5, 1), COMP) shouldBe false
    }

    "An array after standard sorting is sorted" in {
      forAll((genIntArray, "as")) { (as: Array[Int]) =>
        Exercises.isSorted[Int](as.sorted, COMP) shouldBe true
      }
    }

    "A random array is sorted with the ordering where all elements are equal" in {
      forAll((genIntArray, "as")) { (as: Array[Int]) =>
        Exercises.isSorted[Int](as, easyCOMP) shouldBe true
      }
    }

    "Array(1,2,3,4,5,6) is not sorted in inverse order" in {
      Exercises.isSorted(Array(1, 2, 3, 4, 5, 6), invCOMP) shouldBe false
    }

    "Array(6,2,3,4,5,6) is not sorted in inverse order" in {
      Exercises.isSorted(Array(6, 2, 3, 4, 5, 6), invCOMP) shouldBe false
    }

    "Array(1,1,1,1,1,1) is sorted in inverse order" in {
      Exercises.isSorted(Array(1, 1, 1, 1, 1, 1), invCOMP) shouldBe true
    }

    "Array(6,6,4,4,2,1) is sorted in inverse order" in {
      Exercises.isSorted(Array(6, 5, 4, 4, 2, 1), invCOMP) shouldBe true
    }

  }

  "fib" - {

    val genfib = Gen.choose(1, 30)

    "The first Fibonacci number fib(1) is zero" in {
      Exercises.fib(1) shouldBe 0
    }
    "The second Fibonacci number fib(2) is one" in {
      Exercises.fib(2) shouldBe 1
    }
    "The third Fibonacci number fib(3) is one" in {
      Exercises.fib(3) shouldBe 1
    }
    "The fourth Fibonacci number fib(4) is two" in {
      Exercises.fib(4) shouldBe 2
    }

    "Each Fibonacci number is a sum of the two previous numbers" in {
      forAll(genfib -> "n") { (n: Int) =>
        whenever(n > 2) {
          Exercises.fib(n) shouldBe Exercises.fib(n - 1) + Exercises.fib(n - 2)
        }
      }
    }

    "Fibonacci numbers are positive (for positive arguments)" in {
      forAll((genfib -> "n")) { (n: Int) => Exercises.fib(n) should be >= 0 }
    }

  }
}

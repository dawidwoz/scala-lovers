package presentation

import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

class ExercisesSpec
    extends org.scalatest.freespec.AnyFreeSpec
    with org.scalatest.matchers.should.Matchers
    with org.scalatestplus.scalacheck.ScalaCheckPropertyChecks {

  def genTree[A](implicit arbA: Arbitrary[A]): Gen[Tree[A]] =
    Gen
      .frequency(7 -> true, 1 -> false)
      .flatMap { stop =>
        if (stop) arbA.arbitrary map { Leaf(_) }
        else
          for {
            l <- genTree[A]
            r <- genTree[A]
          } yield (Branch(l, r))
      }

  implicit def arbitraryTree[A](implicit
      arbA: Arbitrary[A]
  ): Arbitrary[Tree[A]] =
    Arbitrary[Tree[A]](genTree(arbA))

  implicit def arbitraryOption[A](implicit
      arbA: Arbitrary[A]
  ): Arbitrary[Option[A]] =
    Arbitrary(Gen.oneOf(Gen.const(None), arbA.arbitrary map { Some(_) }))

  def nonEmptyIntList(implicit arb: Arbitrary[Int]): Gen[List[Int]] =
    for {
      n <- Gen.choose(1, 500)
      l <- Gen.listOfN[Int](n, arb.arbitrary)
    } yield (l)

  "Exercise 11 (traverse)" - {

    "some simple scenarios" in {

      def f(n: Int): Option[Int] =
        if (n % 2 == 0) Some(n)
        else None

      ExercisesOption.traverse(List(1, 2, 42))(Some(_)) should
        equal(Some(List(1, 2, 42)))
      ExercisesOption.traverse(List(1, 2, 42))(f) should
        equal(None)
    }

    "traversal of bottom gives bottom" in {
      forAll(nonEmptyIntList -> "l") { (l: List[Int]) =>
        ExercisesOption.traverse(l) { _ => None } should be(None)
      }
    }

    "empty traversal cannot fail" in {
      forAll("f") { (f: Int => Option[Int]) =>
        ExercisesOption.traverse(Nil)(f) should be(Some(Nil))
      }
    }

  }

  "Exercise 10 (sequence)" - {

    "some simple scenarios" in {

      ExercisesOption.sequence(Nil) should
        equal(Some(Nil))
      ExercisesOption.sequence(List(None)) should
        equal(None)
      ExercisesOption.sequence(List(Some(42))) should
        equal(Some(List(42)))
      ExercisesOption.sequence(List(Some(1), Some(2), Some(42))) should
        equal(Some(List(1, 2, 42)))
      ExercisesOption.sequence(List(None, Some(2), Some(42))) should
        equal(None)
      ExercisesOption.sequence(List(Some(1), None, Some(42))) should
        equal(None)
      ExercisesOption.sequence(List(Some(1), Some(2), None)) should
        equal(None)

    }

    "A list without Nones sequences as if this was a list without options" in {
      forAll("l") { (l: List[Int]) =>
        val ol = l map { Some(_) }
        ExercisesOption.sequence(ol) should equal(Some(l))
      }
    }

    "A list with None sequences to None" in {
      forAll("l") { (l: List[Option[Int]]) =>
        val ol = l ++ (None :: l)
        ExercisesOption.sequence(ol) should equal(None)
      }
    }
  }

  "Exercise 9 (map2)" - {

    "any None gives None" in {
      forAll("o", "f") { (o: Option[Int], f: (Int, Int) => Int) =>
        ExercisesOption.map2(o, None)(f) should equal(None)
        ExercisesOption.map2(None, o)(f) should equal(None)
        ExercisesOption.map2(None, None)(f) should equal(None)
      }
    }

    "two Some elements are nicely merged by subtraction" in {
      forAll("n", "m") { (n: Int, m: Int) =>
        ExercisesOption.map2(Some(n), Some(m))(_ - _) should be(Some(n - m))
      }
    }

    "two Some elements are nicely merged by arbitrary f" in {
      forAll("n", "m", "f") { (n: Int, m: Int, f: (Int, Int) => Int) =>
        ExercisesOption.map2(Some(n), Some(m))(f) should be(Some(f(n, m)))
      }
    }
  }

  "Exercise 8 (variance)" - {

    "simple fixed scenarios" in {
      ExercisesOption.variance(List(42, 42, 42)) should be(Some(0.0))
      ExercisesOption.variance(Nil) should be(None)
    }

    "Variance of a singleton is always zero" in {

      // Very large numbers lead to overflows so limit them
      val any_x: Gen[Double] = Gen.choose[Double](-10000.0, 10000.0)

      forAll(any_x -> "x") { (x: Double) =>
        (ExercisesOption.variance(List(x))) should be(Some(0.0))
      }

    }

    "A constant list x,x,x,x,... has variance zero" in {

      // Very long lists kill memory so we choose shorter.
      val max_n = Gen.choose(2, 1000)

      // Very large numbers lead to overflows so limit them
      val any_x: Gen[Double] = Gen.choose[Double](-10000000.0, 10000000.0)

      // Scalacheck will ignore the above generators when shrinking so we
      // deactivate shrinking
      import org.scalacheck.Shrink
      implicit val noShrink: Shrink[Int] = Shrink.shrinkAny

      forAll(any_x -> "x", max_n -> "n") { (x: Double, n: Int) =>
        val v = ExercisesOption.variance(List.fill(n)(x))
        v.asInstanceOf[Some[Double]].get should be <= 0.001
      }
    }

    "-x, x, -x, x, ... has variance x*x" in {

      // Very long lists kill memory so we choose shorter.
      val gen_n = Gen.choose(1, 500)

      // Very large numbers lead to overflows so limit them
      val any_x: Gen[Double] = Gen.choose[Double](.001, 10000.0)

      // Scalacheck will ignore the above generators when shrinking so we
      // deactivate shrinking
      import org.scalacheck.Shrink
      implicit val noShrink: Shrink[Int] = Shrink.shrinkAny

      forAll(any_x -> "x", gen_n -> "n") { (x: Double, n: Int) =>
        val l = List.fill(n)(List(-x, x)) flatMap identity
        val Some(v) = ExercisesOption.variance(l)
        (Math.abs(v - x * x)) should be <= 0.0001
      }

    }

  }

  "Exercise 7 (headGrade)" - {

    "headAge of empty list should be None" in {
      ExercisesOption.headGrade(Nil) should be(None)
    }

    "the integer from the first pair must be correctly extracted and wrapped into Some" in {
      forAll("tail") { (tail: List[(String, Int)]) =>
        forAll("name") { (name: String) =>
          forAll("grade") { (grade: Int) =>
            ExercisesOption.headGrade((name, grade) :: tail) should
              be(Some(grade))
          }
        }
      }
    }

  }

  "Exercise 6 (Option basics)" - {

    "Option map does nothing on None" in {
      forAll("f") { (f: Int => Int) =>
        (None map f) should equal(None)
      }
    }

    "Option map on Some just maps the element" in {
      forAll("n", "f") { (n: Int, f: Int => Int) =>
        (Some(n) map f) should equal(Some(f(n)))
      }
    }

    "Option getOrElse on None always does else" in {
      forAll("m") { (m: Int) =>
        (None getOrElse m) should equal(m)
      }
    }

    "Option getOrElse on Some(n) always gives n" in {
      forAll("n", "m") { (n: Int, m: Int) =>
        (Some(n) getOrElse m) should equal(n)
      }
    }

    "Option flatMap on None does None" in {
      forAll("f") { (f: Int => Option[Int]) =>
        (None flatMap f) should equal(None)
      }
    }

    "Option flatMap to None gives None" in {
      forAll("o") { (o: Option[Int]) =>
        (o flatMap { _ => None }) should equal(None)
      }
    }

    "Option flatMap on Some applies the function or fails" in {
      forAll("n", "f") { (n: Int, f: Int => Option[Int]) =>
        (Some(n) flatMap f) should equal(f(n))
      }
    }

    "Option filter on None gives None" in {
      forAll("p") { (p: Int => Boolean) =>
        (None filter p) should equal(None)
      }
    }

    "Option filter with constant false predicate gives None" in {
      forAll("o") { (o: Option[Int]) =>
        (o filter { _ => false }) should equal(None)
      }
    }

    "Option filter picks the value iff it satisfies the predicate" in {
      forAll("n", "p") { (n: Int, p: Int => Boolean) =>
        (Some(n) filter p) should equal(if (p(n)) Some(n) else None)
      }
    }

  }

  "Exercise 4 (Tree map)" - {

    "a simple scenario test" in {
      val t4 = Branch(Leaf(1), Branch(Branch(Leaf(2), Leaf(3)), Leaf(4)))
      val t5 =
        Branch(Leaf("1"), Branch(Branch(Leaf("2"), Leaf("3")), Leaf("4")))
      Tree.map(t4)(_.toString) should be(t5)
      Tree.map(t4)(x => x) should be(t4)
    }

    "identity is a unit with map" in {
      forAll("t") { (t: Tree[Int]) =>
        Tree.map(t)(x => x) should be(t)
      }
    }

    "map does not change size" in {
      forAll("t", "f") { (t: Tree[Int], f: Int => Int) =>
        val t1 = Tree.map(t)(f)
        Tree.size(t1) should equal(Tree.size(t))
      }
    }

    "map is 'associative'" in {
      forAll("t", "f", "g") { (t: Tree[Int], f: Int => Int, g: Int => Int) =>
        val t1 = Tree.map(Tree.map(t)(f))(g)
        val t2 = Tree.map(t)(g compose f)
        (t1) should equal(t2)
      }

    }
  }

  "Exercise 3 (Tree maximum)" - {

    "a simple scenario test" in {
      Tree.maximum(Branch(Leaf(1), Leaf(2))) should be(2)
    }

    "a singleton tree test" in {
      forAll("n") { (n: Int) => Tree.maximum(Leaf(n)) should be(n) }
    }

    "a bi-node tree test" in {
      forAll("n", "m") { (n: Int, m: Int) =>
        Tree.maximum(Branch(Leaf(m), Leaf(n))) should be(n max m)
      }
    }

    "a tri-node tree test" in {
      forAll("n", "m", "o") { (n: Int, m: Int, o: Int) =>
        val M = n max m max o
        Tree.maximum(Branch(Leaf(m), Branch(Leaf(n), Leaf(o)))) should be(M)
        Tree.maximum(Branch(Leaf(m), Branch(Leaf(n), Leaf(o)))) should be(M)
      }
    }
  }

  "Exercise 2 (Tree size)" - {

    "a simple scenario test" in {
      Tree.size(Branch(Leaf(1), Leaf(2))) should be(3)
    }

    "a leaf should be size 1" in {
      forAll("n") { (n: Int) => Tree.size(Leaf(n)) should be(1) }
    }

  }

  "Exercise 1" - {

    import java.awt.Point

    implicit def arbitraryPoint(implicit
        arb: Arbitrary[Int]
    ): Arbitrary[Point with OrderedPoint] =
      Arbitrary(for {
        x <- arb.arbitrary
        y <- arb.arbitrary
      } yield new Point(x, y) with OrderedPoint)

    "the scenario test from the exercise text" in {

      val p = new Point(0, 1) with OrderedPoint
      val q = new Point(0, 2) with OrderedPoint
      (p < q) shouldBe true
    }

    "non-negative shift preserves order" in {

      val coord = Gen.choose(-10000, +10000)
      val delta = Gen.choose(0, 10000)

      forAll(coord -> "x", coord -> "y", delta -> "a", delta -> "b") {
        (x, y, a, b) =>
          val p = new Point(x, y) with OrderedPoint
          val q = new Point(x + a, y + b) with OrderedPoint
          (p <= q) should be(true)
          (p > q) should be(false)
      }

    }

    "(<) is a total order" in {

      forAll("x", "y") {
        (x: Point with OrderedPoint, y: Point with OrderedPoint) =>
          whenever(x < y) { (y < x) should be(false) }
          whenever(x < y) { (y > x) should be(true) }
          (x < y || y < x || x == y) should be(true)
          (x === x) should be(true)
          (x <= x) should be(true)
          (x >= x) should be(true)
      }

    }

  }

}

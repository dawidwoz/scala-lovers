// Based on Advanced Programming, A. Wąsowski, IT University of Copenhagen

package presentation

// Exercise  1 - compare java Points in Scala

trait OrderedPoint extends scala.math.Ordered[java.awt.Point] {

  this: java.awt.Point =>

  override def compare (that: java.awt.Point): Int =  {
    if ((this.x > that.x) || (this.x == that.x && this.y > that.y)) 1
    else if ((this.x == that.x) && (this.y == that.y)) 0
    else -1
  }

}

sealed trait Tree[+A]
case class Leaf[A] (value: A) extends Tree[A]
case class Branch[A] (left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree {

  // Exercise 2 - return size of a tree

  def size[A] (t: Tree[A]): Int = t match {
    case Leaf(_) => 1
    case Branch(left, right) => size(left) + size(right) + 1
  }

  // Exercise 3 - return max value of a tree

  def maximum (t: Tree[Int]): Int = t match {
    case Leaf(x) => x
    case Branch(left, right) => maximum(left) max maximum(right)
  }

  // Exercise 4 - Map tree

  def map[A,B] (t: Tree[A]) (f: A => B): Tree[B] = t match {
    case Leaf(x) => Leaf(f(x))
    case Branch(left, right) => Branch(map(left)(f), map(right)(f))
  }

}

sealed trait Option[+A] {

  // Exercise 6 - map Option, getOrElse, flatMap and filter

  def map[B] (f: A=>B): Option[B] = this match {
    case None => None
    case Some(x) => Some(f(x))
  }

  /**
   * Ignore the arrow (=>) in default's type below for now.
   * It prevents the argument "default" from being evaluated until it is needed.
   */

  def getOrElse[B >: A] (default: => B): B = this match {
    case None => default
    case Some(x) => x
  }

  def flatMap[B] (f: A => Option[B]): Option[B] = this match {
     case None => None
     case Some(x) => f(x)
  }

  def filter (p: A => Boolean): Option[A] = this match {
    case None => None
    case Some(x) => {
      if (p(x)) Some(x) else None 
    }
  }

}

case class Some[+A] (get: A) extends Option[A]
case object None extends Option[Nothing]

object ExercisesOption {

  def mean(xs: Seq[Double]): Option[Double] =
    if (xs.isEmpty) None
    else Some (xs.sum / xs.length)

  // Exercise 7 - head grade

  def headOption[A] (lst: List[A]): Option[A] = lst match {
    case Nil => None
    case h ::t => Some (h)
  }

  def headGrade (lst: List[(String,Int)]): Option[Int] = {
    //headOption[(String,Int)] (lst).map((option) => option._2)
    for {
      option <- headOption[(String,Int)] (lst)
    } yield {
      option._2
    }
  }

  // Exercise 8

  def variance (xs: Seq[Double]): Option[Double] = {
    for {
      m <- mean(xs)
      value <- mean(xs.map(x => (math.pow(x - m, 2))))
    } yield {
      value
    }
  loop
  }

  // Exercise 9

  def map2[A,B,C] (ao: Option[A], bo: Option[B]) (f: (A,B) => C): Option[C] = {
    //ao.flatMap(a => bo.map(b => f(a,b)))
    for {
      a <- ao
      b <- bo
    } yield {
      f(a, b)
    }
  }

  // Exercise 10

  def sequence[A] (aos: List[Option[A]]): Option[List[A]] = {
    aos.foldRight[Option[List[A]]](Some(Nil))((a,b) => map2(a, b)({ (c, d) => c :: d }))
  }

  // Exercise 11 

  def traverse[A,B] (as: List[A]) (f: A => Option[B]): Option[List[B]] = {
    as.foldRight[Option[List[B]]](Some(Nil))((a,b) => map2(f(a), b)({ (c, d) => c :: d }))
  }

}

// Based on Advanced Programming, Exercises by A. Wąsowski, IT University of Copenhagen

package exercises

trait  ExercisesInterface {

  def fib (n: Int): Int

  def isSorted[A] (as: Array[A], ordered: (A,A) =>  Boolean) :Boolean

  def curry[A,B,C] (f: (A,B) => C): A => (B => C)

  def compose[A,B,C] (f: B => C, g: A => B): A => C

}

object Exercises extends App with ExercisesInterface {

  // Exercise 2 - Create a function n square of a given number 
  def square(n: Int): Int = ???

  // Exercise 3 - Create a function to calculate the fibonacci sequence for a given n, 
  // Hint: use recursion, remember you need to termiante it somehow
  def fib (n: Int): Int = ???

  // Exercise 4 - Create a function to check if array is sorted by this function
  // Hint: remember you can create a local function inside the function
  def isSorted[A] (as: Array[A], comparison: (A,A) =>  Boolean): Boolean = ???

  // Exercise 5 - Create a function that takes two functions that have a common type subset. Compose it to one function. 
  def compose[A,B,C] (f: B => C, g: A => B) : A => C = ???

  // Exercise 6 - Create a function that takes another function with two arguments filling only the first argument
  def curry[A,B,C] (f: (A,B)=>C): A => (B => C) = ???

  // Exercise 1 - Change this line to print first given argument of any type and second option of type String
  println("Hello world!")

}

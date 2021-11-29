// Based on Advanced Programming, Exercises by A. Wąsowski, IT University of Copenhagen

package presentation

trait ExercisesInterface {

  def uncurry[A,B,C] (f: A => B => C): (A, B) => C

}

object Exercises extends ExercisesInterface{
  def uncurry[A, B, C] (f: A =>B => C): (A,B) => C = (a, b) => f(a)(b)
  
  @main def test(): Unit = {
    val curryFunction: String => String => String = a => b => a + b
    // val partialFunction = curryFunction ("Daw")
    // println(partialFunction("id"))
    val uncurryFunction = uncurry (curryFunction)
    println(uncurryFunction("Daw", "id"))
  }
}

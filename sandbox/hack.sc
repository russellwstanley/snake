
object MyStringPimper{

  class PimpedString(s : String){
    def pimped = s + " has been pimped"
  }

  implicit def pimpString(s : String) = new PimpedString(s)



}

object App {
  import MyStringPimper._

  //prints "this string has been pimped"
  def doStuff() : Unit = println("this string" pimped)
}




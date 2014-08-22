//normal inheritance
class Animal(val mass : Int)
class Person(val personMass :Int,val name:String) extends Animal(personMass)

//define a generic type that is restricted to
//Animal and Person as the parameter type
class SocialGroup[T <: Animal]()

//val animalSocialGroup = new SocialGroup[Animal]()
//val humanSocialGroup = new SocialGroup[Person]
//this will fail because  int is not a supertype  of animal
//val mathsClub = new SocialGroup[Int]

//you can extend a generic class but you have to respect the type bounds
class AlturisticSocialGroup[T <: Animal ] extends SocialGroup[T]

//therefore this does not compile
//class AlturisticSocialGroup[T] extends SocialGroup[T]

class Society[T <: SocialGroup[B] ]()




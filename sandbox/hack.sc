val l = List("a","b","c","d","e","f")
l.fo
def appendToMap(map : Map[Int,List[String]], elem : String) : Map[Int,List[String]] = {
  map.size match {
    case 0 => Map(0-> List(elem))
    case _ => {


    }
  }
  val mapIndex = map.size -1
  val workingList = map(mapIndex)
  if(workingList.size < 3) map + ( mapIndex -> (workingList :+ elem))
  else map + (mapIndex + 1 ->  List(elem))

}

 def processList(list : List[String]) : Map[Int,List[String]] = {
     list.tail.foldLeft(Map(0->List(list.head))){ (acc,elem) => {
         val mapIndex = acc.size -1
         val workingList = acc(mapIndex)
         if(workingList.size < 3) acc + ( mapIndex -> (workingList :+ elem))
         else acc + (mapIndex + 1 ->  List(elem))
        }
    }
  }


//processList(List("a"))
//
val f = Map((1->"a"))
val res = f.getOrElse(1,3)
//val p = f(0)

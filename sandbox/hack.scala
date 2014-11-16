val map = scala.collection.mutable.Map[Int,Int](1->2,3->4,4->5)
println(map)
val ret = for(key <- map.keys) yield {
  val value = map(key)
  map += (key -> (value -1))
  value
}

println(ret)
println(map)


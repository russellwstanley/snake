
var functions = Map[String,()=>Int]()
for( i <- 0 until 10 ){
  functions = functions + ("function"+i -> (() => i + 1))

}
println(functions("function0")())
println(functions("function1")())


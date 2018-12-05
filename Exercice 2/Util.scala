object Util {
  def calculerNroAleatoire(a: Int, b:Int): Int ={
    return a + (Math.random * ((a - b) + 1)).asInstanceOf[Int]
  }


  def calculeDistance(x: Float, y:Float, x1:Float,y1: Float): Float = {
    return  (Math.sqrt(Math.pow((x1-x),2)+Math.pow((y1-y),2))).toFloat
    // return 4F
  }

}
import scala.collection.mutable.ArrayBuffer


class Creature(val nom : String, val equipe:Int, var ac: Int, var hp:Int, var regeneration:Int,
               var x:Int, var y:Int, var vivant: Boolean = true, var attaques: Map[Int,Attaque],val deplacement:Int) extends Serializable {

  override def toString = s" $nom ac $ac hp $hp "
}
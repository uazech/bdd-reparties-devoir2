package exercice2.creatures

import exercice2.{Attaque, Creature, Util}

//Attaque(val nom : String, val niveauxAttaques : Map[Int,Int], val damage:Map[String,Int],
//val distanceAttaqueMin: Int,val distanceAttaqueMax: Int
class BarbaresOrc() extends Creature("BarbaresOrc", 2, 17, 142, 0, 142, Util.calculerNroAleatoire(0,100), Util.calculerNroAleatoire(0,100),
  true,
  List(
    new Attaque("orc double axe", Map(1 -> 19, 2 -> 14, 3 -> 9), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 10),
      0, 10),

    new Attaque("orc double axe", Map(1 -> 17, 2 -> 12, 3 -> 7), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 7),
      0, 10),
    new Attaque("orc double axe", Map(1 -> 16, 2 -> 11, 3 -> 6), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 6),
      0, 20)
  )
  ,40)
{
  this.color=(23,47,25)
}
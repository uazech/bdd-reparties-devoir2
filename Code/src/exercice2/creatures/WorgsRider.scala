package exercice2.creatures

import exercice2.{Attaque, Creature, Util}

//Attaque(val nom : String, val niveauxAttaques : Map[Int,Int], val damage:Map[String,Int],
//val distanceAttaqueMin: Int,val distanceAttaqueMax: Int
class WorgsRider() extends Creature(
  "Orc worg rider",
  2,
  18,
  13,
  0,
  13,
  Util.calculerNroAleatoire(150,200), Util.calculerNroAleatoire(150,200),
  true,
   List(new Attaque("battleaxe",Map(1 -> 6), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 2),
    0, 10),

     new Attaque("shortbow",  Map(1 -> 4), Map("numInf" -> 1, "numMax" -> 6, "constant" -> 0),
       5, 10)
   )
  ,30)
{
  this.color=(29,187,245)
}
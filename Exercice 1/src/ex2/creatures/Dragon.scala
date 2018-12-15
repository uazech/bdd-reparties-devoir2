package ex2.creatures

import ex2.{Attaque, Creature, Util}

class Dragon(isFlying:Boolean, isDeguise:Boolean) extends Creature(
  "Dragon",
  2,
  39,
  449,
  0,
  Util.calculerNroAleatoire(0,100), Util.calculerNroAleatoire(0,100),
  true,
  List(new Attaque("Melee bite", Map(1->25), Map("numInf" -> 2, "numMax" -> 8, "constant" -> 15), 0, 2)
  , new Attaque("Claws", Map(1->25), Map("numInf" -> 2, "numMax" -> 6, "constant" -> 2*10), 0, 2)
  , new Attaque("Wings", Map(1->23), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 2*5), 0, 2)
  , new Attaque("Tail Slap", Map(1->23), Map("numInf" -> 2, "numMax" -> 6, "constant" -> 15), 0, 2)
  , new Attaque("Breath weapon", Map(1->24, 2->24, 3->24), Map("numInf" -> 10, "numMax" -> 12, "constant" -> 6),
      10, 50)




  ),40){

}

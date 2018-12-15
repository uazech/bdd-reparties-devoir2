package ex2.creatures

import ex2.{Attaque, Creature, Util}

class MovanicDeva() extends Creature(
  "Movanic Deva",
  1,
  24,
  126,
  0,
  Util.calculerNroAleatoire(150,200), Util.calculerNroAleatoire(150,200),
  true,
  List(new Attaque("Greatsword", Map(1->17, 2->12,3->7), Map("numInf" -> 2, "numMax" -> 6, "constant" -> 9), 0, 2)





  ),40){

}

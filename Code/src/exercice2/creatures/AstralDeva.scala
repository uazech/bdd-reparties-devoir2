package exercice2.creatures

import exercice2.{Attaque, Creature, Util}

class AstralDeva() extends Creature(
  "Astral Deva",
  1,
  29,
  172,
  0,
  172,
  Util.calculerNroAleatoire(150,200), Util.calculerNroAleatoire(150,200),
  true,
  List(new Attaque("Warhammer", Map(1->26, 2->21,3->16), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 14), 0, 2)





  ),40){
  this.heal=50
  this.color=(22,212,233)
}

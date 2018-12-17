package ex2.creatures

import ex2.{Attaque, Creature, Util}

class AngelSlayer() extends Creature(
  "Angel slayer",
  2,
  26,
  112,
  0,
  112,
  Util.calculerNroAleatoire(150,200), Util.calculerNroAleatoire(150,200),
  true,
  List(new Attaque("double axe", Map(1->21, 2->16,3->11), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 7), 0, 2),
    new Attaque("bow", Map(1->19, 2->14,3->9), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 7), 10, 50)





),40){
  this.heal=50


}

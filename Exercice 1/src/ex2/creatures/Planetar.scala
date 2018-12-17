package ex2.creatures

import ex2.{Attaque, Creature, Util}

class Planetar() extends Creature(
  "Plantetar",
  1,
  32,
  229,
  10,
  229,
  Util.calculerNroAleatoire(150,200), Util.calculerNroAleatoire(150,200),
  true,
  List(new Attaque("Holy greatsword", Map(1->27, 2->22, 3->17), Map("numInf" -> 3, "numMax" -> 6, "constant" -> 15), 10, 1)




  ),30){
  this.regeneration=15
  this.heal=50


}

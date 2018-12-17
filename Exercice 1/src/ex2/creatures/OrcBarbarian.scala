package ex2.creatures

import ex2.{Attaque, Creature, Util}

class OrcBarbarian() extends Creature(
  "Orc barbarian",
  2,
  15,
  42,
  0,
  442,
  Util.calculerNroAleatoire(0,100), Util.calculerNroAleatoire(0,100),
  true,
  List(new Attaque("greataxe", Map(1->11), Map("numInf" -> 1, "numMax" -> 12, "constant" -> 10), 0, 2)





),30){

  this.color=(100,14,100)
  override def attaqueCible(): Int = {
    if(Util.calculerNroAleatoire(0,20) == 20){ // Si le dés vaut 20, alors on attaque sans se poser la question de l'armure
      return attaque.calculerDamage()
    }
    super.attaqueCible()
  }




}

package ex2.creatures

import ex2.{Attaque, Creature, Util}

class Dragon() extends Creature(
  "Dragon",
  2,
  39,
  449,
  0,
  449,
  Util.calculerNroAleatoire(0,100), Util.calculerNroAleatoire(0,100),
  true,
  List(new Attaque("Melee bite", Map(1->25), Map("numInf" -> 2, "numMax" -> 8, "constant" -> 15), 0, 2)
  , new Attaque("Claws", Map(1->25), Map("numInf" -> 2, "numMax" -> 6, "constant" -> 2*10), 0, 2)
  , new Attaque("Wings", Map(1->23), Map("numInf" -> 1, "numMax" -> 8, "constant" -> 2*5), 0, 2)
  , new Attaque("Tail Slap", Map(1->23), Map("numInf" -> 2, "numMax" -> 6, "constant" -> 15), 0, 2)
  , new Attaque("Breath weapon", Map(1->24, 2->24, 3->24), Map("numInf" -> 10, "numMax" -> 12, "constant" -> 6),
      10, 50)




  ),40){


  @Override override def seDeplacer(): Unit = {
    if(numStrategie == 1) {
      this.cible = listEnnemis(0) // On se déplace vers le Solar si il est encore en vie. Sinon, on se déplace vers d'autres créatures
      listEnnemis.find(cible => cible.nom == "Solar") match {
        case Some(solar) => this.cible = solar
        case None => this.cible = listEnnemis(0)
      }
    }

    val distanceInitiale = distanceEntre(this.x, this.y, cible.x, cible.y)
    val direction = calculerDirection(this.x, this.y, cible.x, cible.y)
    val distanceApresMouvement = distanceEntre(0, 0, direction._1 * deplacement, direction._2 * deplacement)
    if(distanceInitiale<distanceApresMouvement){
      this.x=cible.x
      this.y=cible.y
    }
    else{
      this.x += direction._1 * deplacement
      this.y += direction._2 * deplacement
    }

  }

  override def attaqueCible(): Int = {
    if(cible.nom=="Solar" && this.x==cible.x && this.y==cible.y){
      isDeguise=false
      isEnVol==true
      numStrategie=2
    }
    super.attaqueCible()

  }
}
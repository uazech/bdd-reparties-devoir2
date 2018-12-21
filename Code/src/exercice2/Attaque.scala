package exercice2

class  Attaque(val nom : String, val niveauxAttaques : Map[Int,Int], val damage:Map[String,Int],
              val distanceAttaqueMin: Int,val distanceAttaqueMax: Int )  extends Serializable{

  val MIN_VALEUR_TOUCHER = 1
  val MAX_VALEUR_TOUCHER = 20

  def calculerAttaque(nroAttaqueCourrant1:Int ): Int ={

    if (niveauxAttaques.size>=nroAttaqueCourrant1)
    return Util.calculerNroAleatoire(MIN_VALEUR_TOUCHER, MAX_VALEUR_TOUCHER)+niveauxAttaques(nroAttaqueCourrant1)
    else
      return 0
  }


  def calculerDamage( ): Int ={
      return Util.calculerNroAleatoire(damage.get("numInf").get,damage.get("numMax").get )+damage.get("constant").get
  }

}

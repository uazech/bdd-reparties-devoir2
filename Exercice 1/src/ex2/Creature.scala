package ex2

import scala.collection.immutable.ListMap
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


class Creature(val nom : String, val equipe:Int, var ac: Int, var hp:Int, var regeneration:Int, var maxHP:Int,
               var x:Double, var y:Double, var vivant: Boolean = true, var attaques: List[Attaque],val deplacement:Int) extends Serializable {


  var listAmis: ListBuffer[Creature]=ListBuffer.empty[Creature]
  var listEnnemis: List[Creature] = List.empty[Creature]
  var id :Int=0
  var cible:Creature=null
  var lastAttaque = -1
  var nbLastAttaque =0
  var isEnVol:Boolean=false
  var isDeguise:Boolean=false
  var attaque:Attaque=null


  var numStrategie = 1

  override def toString = s" $nom ac $ac hp $hp "

  def recevoirAttaques(degats: Int) ={
    this.hp-=degats
  }



  /**
    * Déplace le monstre vers l'ennemi le plus proche
    */
  def seDeplacer(){
    this.cible=listEnnemis(0) // On se déplace vers le plus proche
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

  /**
    * Renvoie les dégats infligés à une cible
    */
  def attaqueCible(): Int ={
    var action = false
    var distance=distanceEntre(this.x,this.y,cible.x,cible.y)
    var nroAttaque = 1
    var i=1
    attaques.foreach { attaque: Attaque =>
      if(distance<=attaque.distanceAttaqueMax && distance>=attaque.distanceAttaqueMin) {
        this.attaque=attaque
        if (i == lastAttaque) {
          nbLastAttaque += 1
        }
        else {
          nbLastAttaque = 1
        }
        lastAttaque = i
        var penetrationArmure = 0
        var calculAttaque=0
        if(nbLastAttaque >= attaque.niveauxAttaques.size) {
          calculAttaque=attaque.calculerAttaque(attaque.niveauxAttaques.last._2)
          penetrationArmure = cible.ac-calculAttaque
        }
        else{
          calculAttaque=attaque.calculerAttaque(nbLastAttaque)
          penetrationArmure = cible.ac-calculAttaque
        }
        if(penetrationArmure<=0){
          return cible.ac+attaque.calculerDamage // on retourne la portion de ac restant plus le damage
        }
        else
          return calculAttaque
      }
      i += 1
    }
    return 0
  }


  /**
    * Calcule la direction entre deux points
    * @param x1 : la coordonnée x du premier point
    * @param y1 : la coordonnée y du premier point
    * @param x2 : la coordonnée x du second point
    * @param y2 : la coordonnée y du second point
    * @return un tuple indiquant la direction à suivre pour se déplacer, en x et en y
    */
  def calculerDirection(x1:Double, y1:Double, x2:Double,y2:Double):(Double, Double) ={
    val vX = x2 - x1;
    val vY = y2 - y1;
    val length = Math.sqrt((vX * vX) + (vY * vY))
    return (vX/length, vY/length)
  }

  /**
    * Calcule la distance entre deux points
    * @param x1 :  la coordonnée x du premier point
    * @param y1 : la coordonnée y du premier point
    * @param x2 : la coordonnée x du second point
    * @param y2 : la coordonnée y du second point
    * @return la distance entre les deux points
    */
  def distanceEntre(x1:Double, y1:Double, x2:Double,y2:Double): Double ={
    if(cible != null && cible.isEnVol)
      return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2)) + 20 // Si la cible vole, on retourne la distance, plus 20 (attaquable seulement à distance

    return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2))

  }

  def cloner():Creature={
    val result = new Creature(this.nom, this.equipe, this.ac, this.hp, this.regeneration, this.maxHP,
    this.x, this.y, this.vivant, this.attaques,this.deplacement)
    result.cible=this.cible
    result.id=this.id
    result.listEnnemis=this.listEnnemis
    return result
  }

  def seRegenerer()={
    val newHP = hp + regeneration
    if(newHP>maxHP){
      hp=maxHP
    }
    else{
      hp=newHP
    }
  }
}
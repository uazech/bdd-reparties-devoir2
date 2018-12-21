package exercice1

import scala.collection.mutable.ArrayBuffer

class CreatureBO(val name : String) extends Serializable {
  var spells =  ArrayBuffer[String]()
  def addSpell(spell : String) : Unit = {
    if(!spells.contains(spell)) {
      spells += spell
    }
  }


}
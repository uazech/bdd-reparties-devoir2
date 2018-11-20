import scala.collection.mutable.ArrayBuffer

class Creature(val name : String) extends Serializable {
  var spells =  ArrayBuffer[String]()
  def addSpell(spell : String) : Unit = {
    spells += spell
  }


  override def toString = s"Creature()"
}
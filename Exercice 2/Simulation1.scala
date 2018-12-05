
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import testGraph._
import testPetersenGraph.sc
import java.io._
import scala.collection.immutable.ListMap
import scala.util.parsing.json.{JSONObject, JSONArray, JSON}

import scala.concurrent._
import ExecutionContext.Implicits.global


object ControlAttaques { //avec cette classe on controle les etapes des attaques  par tour  +35/+30/+25/+20
  var controlAttaques = Map.empty[String, Int];

  def incluire(cle: String, v: Int) = { //attaque, etape courrant
    controlAttaques = controlAttaques + (cle -> v)
  }

  def isEmpty(): Boolean = { //attaque, etape courrant
    return controlAttaques.isEmpty
  }

  def chercherValue(cle: String): Int = {
    if (controlAttaques.contains(cle))
      return controlAttaques(cle)
    else
      return 0
  }

  def init() = {
    controlAttaques = Map.empty[String, Int];
  }

}

class Simulation1() extends Serializable {
  def sendMessages(ctx: EdgeContext[Creature, Float, (String, Int)]): Unit = {
    var source = ctx.srcAttr
    var destination = ctx.dstAttr
    var distance = ctx.attr

    if (source.equipe != destination.equipe && destination.vivant && source.vivant) {

      var attaques = ListMap(source.attaques.toSeq.sortBy(_._1): _*).toArray //trie attaque par priorité
      var i = 0
      var action = false
      var nroAttaque = 1

      while (i < attaques.length && !action) { //on cherche  1ere attaque disponible par priorite, on attaque et on sort
        if (destination.ac > 0 && distance >= attaques(i)._2.distanceAttaqueMin && distance <= attaques(i)._2.distanceAttaqueMax) { //il faut toucher
          ControlAttaques.synchronized { //les attaques sont partagées entre les sendmesseger (chaque niveau d'attaque est utilié 1 fois par iteration
            nroAttaque = ControlAttaques.chercherValue(ctx.srcId.toString + attaques(i)._1)
            nroAttaque += 1
            val attaqueCalcule = attaques(i)._2.calculerAttaque(nroAttaque)
            ControlAttaques.incluire(ctx.srcId.toString + attaques(i)._1, nroAttaque) //attaque déjà utilisée
            action = true
            ctx.sendToDst(("AC", -attaqueCalcule))
          }
        }
        if (destination.ac <= 0) { //il faut faire des degats
          val attaqueCalcule = attaques(i)._2.calculerDamage()
          action = true
          ctx.sendToDst(("HP", -attaqueCalcule))
        }
        i += 1
      }
    }
  }

  def updateDistances(g: Graph[Creature, Float]): Graph[Creature, Float] = {
    val g1: Graph[Creature, Float] = g.mapTriplets(a => Util.calculeDistance(a.dstAttr.x, a.dstAttr.y, a.srcAttr.x, a.srcAttr.y))
    return g1
  }


  def updateState(vid: VertexId, sommet: Creature, message: (String, Int)): Creature = {

    var ac = sommet.ac
    var hp = sommet.hp
    var vivant = true
    var regeneration = sommet.regeneration
    if (message._1.equalsIgnoreCase("AC")) {
      ac += message._2
    }
    if (message._1.equalsIgnoreCase("HP")) {
      hp += message._2
    }

    if (hp <= 0 && sommet.regeneration <= 0) {
      vivant = false

    }
    if (hp <= 0 && sommet.regeneration > 0) {
      regeneration -= 1

    }
    return new Creature(sommet.nom, sommet.equipe, ac, hp, sommet.regeneration, sommet.x, sommet.y, vivant, sommet.attaques, sommet.deplacement)
  }


  def execute(g: Graph[Creature, Float], maxIterations: Int, sc: SparkContext): Graph[Creature, Float] = {
    var myGraph = g
    var counter = 0
    val fields = new TripletFields(true, true, true) //join strategy


    def loop1: Unit = {
      while (true) {
        myGraph = updateDistances(myGraph)

        println("ITERATION NUMERO : " + (counter + 1))
        ControlAttaques.synchronized { //reset
          ControlAttaques.init()
        }

        counter += 1
        if (counter == maxIterations) return

        val messages = myGraph.aggregateMessages[(String, Int)](
          sendMessages,
          null,
          fields
        )

        if (messages.isEmpty()) return
        myGraph = myGraph.joinVertices(messages)(
          (vid, sommet, bestId) => updateState(vid, sommet, bestId)
        )

        val facts1: RDD[String] =
          myGraph.triplets.map(triplet =>
            triplet.srcAttr + " est a " + triplet.attr + " de distance de:" + triplet.dstAttr)
        facts1.collect.foreach(println(_))
      }
    }
    loop1
    myGraph


  }
}


object doSimulation extends App {
  val MIN_AXE_X = 0
  val MAX_AXE_X = 2000

  val MIN_AXE_Y = 0
  val MAX_AXE_Y = 2000


  val conf = new SparkConf()
    .setAppName("Simulation 1")
    .setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  //<---------------Solar------------------->
  var attaque = Map(1 -> 35, 2 -> 30, 3 -> 25, 4 -> 20)
  var damage = Map("numInf" -> 3, "numMax" -> 6, "constant" -> 18)
  var greatSword = new Attaque("dancing greatSword", attaque, damage,
    5, 10)
  attaque = Map(1 -> 31, 2 -> 26, 3 -> 21, 4 -> 16)
  damage = Map("numInf" -> 2, "numMax" -> 6, "constant" -> 14)
  var longBow = new Attaque("composite longbow", attaque, damage,
    98, 500)
  var setAttaque = Map(1 -> longBow, 2 -> greatSword) // priority, attaque
  var solar = new Creature("Solar", 1, 44, 363, 15, 15, 1, true, setAttaque, 50)


  //<---------------worg rider------------------->
  attaque = Map(1 -> 6)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 2)
  var battleaxe = new Attaque("battleaxe", attaque, damage,
    5, 10)
  attaque = Map(1 -> 4)
  damage = Map("numInf" -> 1, "numMax" -> 6, "constant" -> 0)
  var shortbow = new Attaque("shortbow", attaque, damage,
    5, 10)
  setAttaque = Map(1 -> battleaxe, 2 -> shortbow) // priority, attaque
  var worgRider = new Creature("Orc worg rider", 2, 18, 13, 0, 100, 50, true, setAttaque, 20)

  //<---------------double axe------------------->
  attaque = Map(1 -> 19, 2 -> 14, 3 -> 9)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 10)
  var doubleaxe1 = new Attaque("orc double axe", attaque, damage,
    5, 10)
  attaque = Map(1 -> 17, 2 -> 12, 3 -> 7)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 7)
  var doubleaxe2 = new Attaque("orc double axe", attaque, damage,
    5, 10)
  attaque = Map(1 -> 16, 2 -> 11, 3 -> 6)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 6)
  var longbow = new Attaque("orc double axe", attaque, damage,
    5, 20)
  setAttaque = Map(1 -> doubleaxe1, 1 -> doubleaxe2, 2 -> shortbow) // priority, attaque
  var doubleaxe = new Creature("double axe fury", 2, 17, 142, 0, 80, 30, true, setAttaque, 40)

  //<---------------brutal warlord------------------->

  attaque = Map(1 -> 20, 2 -> 15, 3 -> 10)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 10)
  var viciousFlail = new Attaque("vicious flail", attaque, damage,
    5, 10)
  attaque = Map(1 -> 19)
  damage = Map("numInf" -> 1, "numMax" -> 6, "constant" -> 5)
  var throwingaxe = new Attaque(" throwing axe ", attaque, damage,
    5, 20)
  setAttaque = Map(1 -> throwingaxe, 1 -> viciousFlail) // priority, attaque
  var brutalWarlord = new Creature("brutal Warlord", 2, 27, 141, 0, 110, 47, true, setAttaque, 30)
  var myVertices = sc.makeRDD(Array(
    (1L, solar),
    (2L, worgRider),
    (3L, doubleaxe),
    (4L, brutalWarlord)
  ))

  var myEdges = sc.makeRDD(Array(
    Edge(1L, 2L, 0F), Edge(1L, 3L, 0F), Edge(1L, 4L, 0F), Edge(2L, 1L, 0F)
  ))

  var myGraph = Graph(myVertices, myEdges)
  val simulation1 = new Simulation1()
  val res = simulation1.execute(myGraph, 20, sc)

}

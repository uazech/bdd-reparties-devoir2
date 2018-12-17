package ex2

import org.apache.spark.graphx._
import org.apache.spark.{SparkConf, SparkContext}
import ex2.creatures._

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
  sc.setCheckpointDir(System.getProperty("user.dir") + "\\out")


  //<---------------Solar (1)------------------->
  var attaque = Map(1 -> 35, 2 -> 30, 3 -> 25, 4 -> 20)
  var damage = Map("numInf" -> 3, "numMax" -> 6, "constant" -> 18)
  var greatSword = new Attaque("dancing greatSword", attaque, damage,
    0, 10)
  attaque = Map(1 -> 31, 2 -> 26, 3 -> 21, 4 -> 16)
  damage = Map("numInf" -> 2, "numMax" -> 6, "constant" -> 14)
  var longBow = new Attaque("composite longbow", attaque, damage,
    98, 500)
  var setAttaque = List(longBow, greatSword) // priority, attaque
  var solar = new Creature("Solar", 1, 44, 363, 15, 363, Util.calculerNroAleatoire(0, 100), Util.calculerNroAleatoire(0, 100), true, setAttaque, 50)
  solar.color = (216, 245, 29)


  //<---------------brutal warlord (1)------------------->

  attaque = Map(1 -> 20, 2 -> 15, 3 -> 10)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 10)
  var viciousFlail = new Attaque("vicious flail", attaque, damage,
    0, 10)
  attaque = Map(1 -> 19)
  damage = Map("numInf" -> 1, "numMax" -> 6, "constant" -> 5)
  var throwingaxe = new Attaque(" throwing axe ", attaque, damage,
    5, 20)
  setAttaque = List(throwingaxe, viciousFlail) // priority, attaque
  var brutalWarlord = new Creature("brutal Warlord", 2, 27, 141, 0, 141, Util.calculerNroAleatoire(0, 100), Util.calculerNroAleatoire(0, 100), true, setAttaque, 30)
  brutalWarlord.color = (124, 14, 120)


  //<---------------Worgs Rider (9)------------------->

  var worgsRider1 = new WorgsRider()
  var worgsRider2 = new WorgsRider()
  var worgsRider3 = new WorgsRider()
  var worgsRider4 = new WorgsRider()
  var worgsRider5 = new WorgsRider()
  var worgsRider6 = new WorgsRider()
  var worgsRider7 = new WorgsRider()
  var worgsRider8 = new WorgsRider()
  var worgsRider9 = new WorgsRider()

  //<---------------Worgs Rider (4)------------------->

  var BarbaresOrc1 = new BarbaresOrc()
  var BarbaresOrc2 = new BarbaresOrc()
  var BarbaresOrc3 = new BarbaresOrc()
  var BarbaresOrc4 = new BarbaresOrc()


  solar.id = 1
  brutalWarlord.id = 2
  worgsRider1.id = 3
  worgsRider2.id = 4
  worgsRider3.id = 5
  worgsRider4.id = 6
  worgsRider5.id = 7
  worgsRider8.id = 8
  worgsRider9.id = 9
  BarbaresOrc1.id = 10
  BarbaresOrc2.id = 11
  BarbaresOrc3.id = 12
  BarbaresOrc4.id = 13


  var myVertices = sc.makeRDD(Array(
    (1L, solar),
    (2L, brutalWarlord),
    (3L, worgsRider1),
    (4L, worgsRider2),
    (5L, worgsRider3),
    (6L, worgsRider4),
    (7L, worgsRider5),
    (8L, worgsRider6),
    (9L, worgsRider8),
    (10L, worgsRider9),
    (11L, BarbaresOrc1),
    (12L, BarbaresOrc2),
    (13L, BarbaresOrc3),
    (14L, BarbaresOrc4)

  ))

  var myEdges = sc.makeRDD(Array(
    Edge(1L, 2L, 2F), Edge(1L, 3L, 2F), Edge(1L, 4L, 2F), Edge(1L, 5L, 2F), Edge(1L, 6L, 2F), Edge(1L, 7L, 2F), Edge(1L, 8L, 2F)
    , Edge(1L, 9L, 2F), Edge(1L, 10L, 2F), Edge(1L, 11L, 2F), Edge(1L, 12L, 2F), Edge(1L, 13L, 2F), Edge(1L, 14L, 2F)
  ))

  var myGraph = Graph(myVertices, myEdges)
  val simulation1 = new Simulateur()
  val res = simulation1.execute(myGraph, 50, sc)

}

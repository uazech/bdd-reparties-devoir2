package ex2


import org.apache.spark.graphx._
import org.apache.spark.{SparkConf, SparkContext}


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
  sc.setCheckpointDir(System.getProperty("user.dir"))


  //<---------------Solar------------------->
  var attaque = Map(1 -> 35, 2 -> 30, 3 -> 25, 4 -> 20)
  var damage = Map("numInf" -> 3, "numMax" -> 6, "constant" -> 18)
  var greatSword = new Attaque("dancing greatSword", attaque, damage,
    0, 10)
  attaque = Map(1 -> 31, 2 -> 26, 3 -> 21, 4 -> 16)
  damage = Map("numInf" -> 2, "numMax" -> 6, "constant" -> 14)
  var longBow = new Attaque("composite longbow", attaque, damage,
    98, 500)
  var setAttaque = List(longBow,  greatSword) // priority, attaque
  var solar = new Creature("Solar", 1, 44, 363, 15, 15, 1, true, setAttaque, 50)


  //<---------------worg rider------------------->
  attaque = Map(1 -> 6)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 2)
  var battleaxe = new Attaque("battleaxe", attaque, damage,
    0, 10)
  attaque = Map(1 -> 4)
  damage = Map("numInf" -> 1, "numMax" -> 6, "constant" -> 0)
  var shortbow = new Attaque("shortbow", attaque, damage,
    5, 10)
  setAttaque = List( shortbow, battleaxe) // priority, attaque
  var worgRider = new Creature("Orc worg rider", 2, 18, 13, 0, 100, 50, true, setAttaque, 20)

  //<---------------double axe------------------->
  attaque = Map(1 -> 19, 2 -> 14, 3 -> 9)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 10)
  var doubleaxe1 = new Attaque("orc double axe", attaque, damage,
    0, 10)
  attaque = Map(1 -> 17, 2 -> 12, 3 -> 7)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 7)
  var doubleaxe2 = new Attaque("orc double axe", attaque, damage,
    5, 10)
  attaque = Map(1 -> 16, 2 -> 11, 3 -> 6)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 6)
  var longbow = new Attaque("orc double axe", attaque, damage,
    5, 20)
  setAttaque = List(doubleaxe1,  doubleaxe2, shortbow) // priority, attaque
  var doubleaxe = new Creature("double axe fury", 2, 17, 142, 0, 80, 30, true, setAttaque, 40)

  //<---------------brutal warlord------------------->

  attaque = Map(1 -> 20, 2 -> 15, 3 -> 10)
  damage = Map("numInf" -> 1, "numMax" -> 8, "constant" -> 10)
  var viciousFlail = new Attaque("vicious flail", attaque, damage,
    0, 10)
  attaque = Map(1 -> 19)
  damage = Map("numInf" -> 1, "numMax" -> 6, "constant" -> 5)
  var throwingaxe = new Attaque(" throwing axe ", attaque, damage,
    5, 20)
  setAttaque = List( throwingaxe, viciousFlail) // priority, attaque
  var brutalWarlord = new Creature("brutal Warlord", 2, 27, 141, 0, 110, 47, true, setAttaque, 30)
  var myVertices = sc.makeRDD(Array(
    (1L, solar),
    (2L, worgRider),
    (3L, doubleaxe),
    (4L, brutalWarlord)
  ))

  var myEdges = sc.makeRDD(Array(
    Edge(1L, 2L, 2F), Edge(1L, 3L, 2F), Edge(1L, 4L, 2F), Edge(2L, 1L, 2F)
  ))

  var myGraph = Graph(myVertices, myEdges)
  val simulation1 = new Simulateur()
  val res = simulation1.execute(myGraph, 50, sc)

}

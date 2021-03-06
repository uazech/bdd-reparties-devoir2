package exercice2

import exercice2.creatures._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer



object doSimulation2 extends App {



  val conf = new SparkConf()
    .setAppName("Simulation 2")
    .setMaster("local[1]") // 1 car plus rapide


  val sc = new SparkContext(conf)
  sc.setCheckpointDir(System.getProperty("user.dir")+"\\out")


  sc.setLogLevel("WARN")
  val arrayCreature: ArrayBuffer[(VertexId, Creature)] = ArrayBuffer()
  //<---------------Solar------------------->
  var attaque = Map(1 -> 35, 2 -> 30, 3 -> 25, 4 -> 20)
  var damage = Map("numInf" -> 3, "numMax" -> 6, "constant" -> 18)
  var greatSword = new Attaque("dancing greatSword", attaque, damage,
    0, 10)
  attaque = Map(1 -> 31, 2 -> 26, 3 -> 21, 4 -> 16)
  damage = Map("numInf" -> 2, "numMax" -> 6, "constant" -> 14)
  var longBow = new Attaque("composite longbow", attaque, damage,
    98, 500)
  var setAttaque = List(longBow, greatSword) // priority, attaque
  var solar = new Creature("Solar", 1, 44, 363, 15, 363, 15, 1, true, setAttaque, 50)
  solar.regeneration=15
  solar.color = (216, 245, 29)
  solar.heal=50
  // Team 1
  solar.id=1
  arrayCreature+=((1, solar))
   var c:Creature = new Planetar
  c.id=2
  arrayCreature+=((2, c))
  c = new Planetar
  c.id=3
  arrayCreature+=((3, c))
  c = new MovanicDeva
  c.id=4
  arrayCreature+=((4, c))
  c = new MovanicDeva
  c.id=5
  arrayCreature+=((5, c))
  c = new AstralDeva
  c.id=6
  arrayCreature+=((6, c))
  c = new AstralDeva
  c.id=7
  arrayCreature+=((7, c))
  c = new AstralDeva
  c.id=8
  arrayCreature+=((8, c))
  c = new AstralDeva
  c.id=9
  arrayCreature+=((9, c))
  c = new AstralDeva
  c.id=9
  arrayCreature+=((10,c))

  // Team 2 :
  val dragon = new Dragon
  dragon.id=11
  arrayCreature+=((11, dragon))
  for(i<-12 to 22){
    c = new AngelSlayer
    c.id=i
    arrayCreature+=((i, c))
  }
  for(i<-23 to 24){
    c = new OrcBarbarian
    c.id=i
    arrayCreature+=((i, c))
  }
  val creatureRdd: RDD[(VertexId, (Creature))] = sc.parallelize(arrayCreature)

  val relationsArray: ArrayBuffer[Edge[Float]] = ArrayBuffer()
//  println(arrayCreature.size)
  for(i<-1 to arrayCreature.length){
    for(j<-i+1 to arrayCreature.length){
//        println(i+" - "+j)
        val amiOuEnnemi = if(arrayCreature(i-1)._2.equipe == arrayCreature(j-1)._2.equipe) 1F else 2F
        relationsArray+=Edge(arrayCreature(i-1)._1.toLong, arrayCreature(j-1)._1.toLong, amiOuEnnemi)
    }
  }
  val relationsRdd: RDD[(Edge[Float])] = sc.parallelize(relationsArray)

  var myGraph = Graph(creatureRdd, relationsRdd)
  val simulation2 = new Simulateur()

  val res = simulation2.execute(myGraph, 200, sc)
//  solar.x=75
//  solar.y=75
////
//  dragon.x=50
//  dragon.y=50
//
//  dragon.listEnnemis=List(solar)
//
//  dragon.seDeplacer()
//  dragon.seDeplacer()
//  println(dragon)
//
//  solar.listEnnemis+=c
//  solar.seDeplacer()
//  solar.attaqueCible()
//  solar.attaqueCible()
//  solar.attaqueCible()
//  solar.attaqueCible()
//  solar.attaqueCible()
//  solar.attaqueCible()
//  println(solar)

}

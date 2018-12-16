package ex2


import java.awt.Color
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io._
import java.lang.management.ManagementFactory
import java.util.Date

import org.apache.spark.SparkContext
import org.apache.spark.graphx._
import org.json4s.DefaultFormats

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag



class Simulateur() extends Serializable {


  def sendEnnemisMessage(ctx: EdgeContext[Creature, Float, List[Creature]]): Unit = {
    val source = ctx.srcAttr
    val destination = ctx.dstAttr
    val relation = ctx.attr
    if(relation==2){ // 2 = ennemis
      source.listEnnemis=null // On reset les ennemis pour ne pas prendre trop de place
      source.cible=null
      destination.listEnnemis=null
      destination.cible=null
      ctx.sendToSrc(List(destination))
      ctx.sendToDst(List(source))
    }

  }

  def sendAttaqueMessage(ctx: EdgeContext[Creature, Float, Int]): Unit = {
    val source = ctx.srcAttr
    val destination = ctx.dstAttr
    if(source.cible.id == destination.id){
      ctx.sendToDst(source.attaqueCible())
    }
    else if (destination.cible.id == source.id){
      ctx.sendToSrc(source.attaqueCible())
    }
    return
  }

  def mergeAttaques(attaque1: Int, attaque2: Int): Int = {
    (attaque1 + attaque2)
  }

  def mergeEnnemisMessage(result: List[Creature], ennemi: List[Creature]): List[Creature] = {
    result:::ennemi
  }

  def joinEnnemisMessages(vid: VertexId, source: Creature, ennemis:  List[Creature]): Creature = {

    source.listEnnemis = ennemis.sortBy((ennemi) => ennemi.distanceEntre(source.x, source.y, ennemi.x, ennemi.y)) // On trie par rapport à la distance
      .filter(creature=>(!creature.isDeguise)) // On ne prend pas en compte les créatures déguisées

    source.seDeplacer()

    val result = new Creature(source.nom, source.equipe, source.ac, source.hp, source.regeneration,
      source.x, source.y, source.vivant, source.attaques,source.deplacement)
    result.cible=source.cible
    result.id=source.id
    result.listEnnemis=source.listEnnemis
    result
  }

  def joinAttaquesMessages(vid: VertexId, source: Creature, degats:  Int): Creature = {
    source.hp-=degats
    val result = new Creature(source.nom, source.equipe, source.ac, source.hp, source.regeneration,
      source.x, source.y, source.vivant, source.attaques,source.deplacement)
    result.cible=source.cible
    result.id=source.id
    result.listEnnemis=source.listEnnemis
    result
  }




  def execute(g: Graph[Creature, Float], maxIterations: Int, sc: SparkContext): Graph[Creature, Float] = {
    var myGraph = g
    var counter = 0
    val fields = new TripletFields(true, true, true) //join strategy


    def loop1: Unit = {
      while (true) {

        println("ITERATION NUMERO : " + (counter + 1))

        counter += 1
        if (counter == maxIterations) return

        // On gère les regénérations
        //TODO

        // On gère les ennemis
        val messagesEnnemis = myGraph.aggregateMessages[ List[Creature]](
          sendEnnemisMessage,
          mergeEnnemisMessage,
          fields
        )

        if (messagesEnnemis.isEmpty()){
          myGraph.vertices.foreach(arg=>println(arg._2))
          return // S'il n'y a pas d'ennemis, on peut arrêter le programme
        }

        myGraph.vertices.foreach(arg=>println(arg._2))

        myGraph = myGraph.joinVertices(messagesEnnemis)(
          (vid, sommet, bestId) => joinEnnemisMessages(vid, sommet, bestId)
        )


        // On gère les attaques
        val messagesAttaques = myGraph.aggregateMessages[ Int](
          sendAttaqueMessage,
          mergeAttaques,
          fields
        )
        myGraph = myGraph.joinVertices(messagesAttaques)(
          (vid, sommet, bestId) => joinAttaquesMessages(vid, sommet, bestId)
        )



        // On gère les demandes et réponses de Heal (Requiert la liste des alliés
        // TODO

        // On supprime les créatures mortes
        myGraph = myGraph.subgraph(vpred = (id, creature) =>  creature.hp > 0)


          myGraph.checkpoint()





      }
    }
//    loop1
    myGraph
  }


}


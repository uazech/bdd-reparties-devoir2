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


  /**
    * Envoie un message indiquant un ennemi aux créatures
    * @param ctx
    */
  def sendEnnemisMessage(ctx: EdgeContext[Creature, Float, List[Creature]]): Unit = {
    val source = ctx.srcAttr
    val destination = ctx.dstAttr
    val relation = ctx.attr
    if(relation==2){ // 2 = ennemis
      source.listEnnemis=null // On reset les informations inutiles pour ne pas prendre trop de place
      source.cible=null
      source.attaque=null
      destination.listEnnemis=null
      destination.cible=null
      destination.attaque=null
      ctx.sendToSrc(List(destination))
      ctx.sendToDst(List(source))
    }

  }

  /**
    * Envoi un message d'attaque à un ennemi (creature.cible)
    * @param ctx
    */
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

  /**
    * Merge les attaques des créatures
    * @param attaque1 la première attaque
    * @param attaque2 la deuxième attaque
    * @return la somme des deux attaques (les dégats infligés)
    */
  def mergeAttaques(attaque1: Int, attaque2: Int): Int = {
    (attaque1 + attaque2)
  }

  /**
    * Merge deux listes de messages indiquant les ennemis
    * @param liste1
    * @param liste2
    * @return une nouvelle liste, mergée
    */
  def mergeEnnemisMessage(liste1: List[Creature], liste2: List[Creature]): List[Creature] = {
    liste1:::liste2
  }

  /**
    * Joint les messages indiquants les ennemis
    * Remplis la liste des ennemis de la créature
    * Déplace la créature vers l'ennemi le plus proche
    * @param vid : l'identifiant du vertex
    * @param source : la créature qu'on souhaite traiter
    * @param ennemis : les ennemis à remplir
    * @return une nouvelle créature identique à la source, qui s'est déplacée vers l'ennemi le plus proche
    */
  def joinEnnemisMessages(vid: VertexId, source: Creature, ennemis:  List[Creature]): Creature = {
    source.listEnnemis = ennemis.sortBy((ennemi) => ennemi.distanceEntre(source.x, source.y, ennemi.x, ennemi.y)) // On trie par rapport à la distance
      .filter(creature=>(!creature.isDeguise)) // On ne prend pas en compte les créatures déguisées
    source.seDeplacer()

    val result = source.cloner()
    result
  }

  /**
    * Fait en sorte que la créature prend les dégats en paramètre
    * Gère aussi la regénération de la créature
    * @param vid : l'id du vertex
    * @param source : la creature qui prend des dégats
    * @param degats : les dégats à infliger
    * @return une nouvelle créature identique à la première, qui a perdu degats hp, et qui s'est regnérée
    */
  def joinAttaquesMessages(vid: VertexId, source: Creature, degats: Int): Creature = {
    if (degats >= source.ac) {
      source.ac = 0
      source.hp -= (degats - source.ac) // calcule la portion de hp
    }
    else
      source.ac -= degats
    source.seRegenerer()
    val result = source.cloner()
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
        myGraph = myGraph.subgraph(vpred = (id, creature) =>  creature.hp >= 0)


          myGraph.checkpoint()





      }
    }
    loop1
    myGraph
  }


}


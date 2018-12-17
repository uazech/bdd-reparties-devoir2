package ex2


import java.io._

import org.apache.spark.SparkContext
import org.apache.spark.graphx._



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
      source.reset // On reset les informations inutiles à envoyer
      destination.reset
      ctx.sendToSrc(List(destination))
      ctx.sendToDst(List(source))
    }
  }

  /**
    * Envoie un message indiquant un ami aux créatures
    * @param ctx
    */
  def sendAmisMessage(ctx: EdgeContext[Creature, Float, List[Creature]]): Unit = {
    val source = ctx.srcAttr
    val destination = ctx.dstAttr
    val relation = ctx.attr
    if(relation==1){ // 1 = amis
      source.reset // On reset les informations inutiles à envoyer
      destination.reset
      ctx.sendToSrc(List(destination))
      ctx.sendToDst(List(source))
    }
  }

  /**
    * Envoie un message indiquant les heals à effectuer.
    * @param ctx
    */
  def sendHealMessage(ctx: EdgeContext[Creature, Float, (Boolean, Int)]): Unit = {
    val source = ctx.srcAttr
    val destination = ctx.dstAttr
    val relation = ctx.attr
    if(relation==1){ // 1 = amis
      if(source.healCible!=null){
        ctx.sendToSrc(true, 0)
        ctx.sendToDst(false, source.heal)
      }
      if(destination.healCible!=null){
        ctx.sendToDst(true, 0)
        ctx.sendToSrc(false, source.heal)
      }
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
  }

  /**
    * Merge les attaques des créatures
    * @param attaque1 la première attaque
    * @param attaque2 la deuxième attaque
    * @return la somme des deux attaques (les dégats infligés)
    */
  def mergeAttaques(attaque1: Int, attaque2: Int): Int = {
    return (attaque1 + attaque2)
  }

  /**
    * Merge deux listes de messages indiquant les ennemis ou les amis
    * @param liste1
    * @param liste2
    * @return une nouvelle liste, mergée
    */
  def mergeAmisEtEnnemisMessage(liste1: List[Creature], liste2: List[Creature]): List[Creature] = {
    liste1:::liste2
  }

  /**
    * Merge les messages de heal
    * @param msg1 : Le premier message de heal
    * @param msg2 : Le second message de heal
    * @return : tuple._1 : true = on supprime le heal, false = on heal.
    *         tuple._2 = la valeur du heal à effectuer
    */
  def mergeHeal(msg1: (Boolean, Int), msg2: (Boolean, Int)): (Boolean, Int) = {
    return (msg1._1||msg2._1, msg1._2+msg2._2) // Deux false = false => on heal, Un false et un true = true => on supprime le heal
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
    source.seDeplacer
    source.identifierCibleHeal // On identifie la cible du heal
    val result = source.cloner()
    return result
  }

  /**
    * Joint les messages indiquants les amis
    * Remplis la liste des amis de la créature
    * @param vid : l'identifiant du vertex
    * @param source : la créature qu'on souhaite traiter
    * @param amis : les ennemis à remplir
    * @return une nouvelle créature identique à la source, qui s'est déplacée vers l'ennemi le plus proche
    */
  def joinAmissMessages(vid: VertexId, source: Creature, amis:  List[Creature]): Creature = {
    source.listAmis = amis.sortBy((ennemi) => ennemi.distanceEntre(source.x, source.y, ennemi.x, ennemi.y)) // On trie par rapport à la distance
    val result = source.cloner()
    return result
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

  /**
    * Joint les messages de heal
    * Supprime le heal si un heal a été effectué
    * Ajoute la valeur du heal aux HP
    * @param vid l'identifiant du vertex
    * @param source la créature à modifier
    * @param tuple _1 : true si on doit supprimer le heal
    *              _2 : la valeur du heal à recevoir
    * @return
    */
  def joinHealMessages(vid: VertexId, source: Creature, tuple: (Boolean, Int)): Creature = {
    if(tuple._1){ // On supprime le heal
      source.heal=0
    }
    source.recevoirHeal(tuple._2)
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

        // On gère les amis
        val messagesAmis = myGraph.aggregateMessages[ List[Creature]](
          sendAmisMessage,
          mergeAmisEtEnnemisMessage,
          fields
        )
        myGraph = myGraph.joinVertices(messagesAmis)(
          (vid, sommet, bestId) => joinAmissMessages(vid, sommet, bestId)
        )


        // On gère les ennemis
        val messagesEnnemis = myGraph.aggregateMessages[ List[Creature]](
          sendEnnemisMessage,
          mergeAmisEtEnnemisMessage,
          fields
        )

        if (messagesEnnemis.isEmpty()){
          myGraph.vertices.foreach(arg=>println(arg._2))
          return // S'il n'y a pas d'ennemis, on peut arrêter le programme
        }

        myGraph.vertices.foreach(arg=>
          println(arg._2))

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



        // On gère les demandes et réponses de Heal
        val messagesHeal = myGraph.aggregateMessages[ (Boolean, Int)](
          sendHealMessage,
          mergeHeal,
          fields
        )
//        myGraph = myGraph.joinVertices(messagesHeal)(
//          (vid, sommet, bestId) => joinHealMessages(vid, sommet, bestId)
//        )

        // On supprime les créatures mortes
        myGraph = myGraph.subgraph(vpred = (id, creature) =>  creature.hp > 0)

          myGraph.checkpoint()





      }
    }
    loop1
    myGraph
  }


}


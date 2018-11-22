
import java.io.File

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.io.Source
import scala.util.matching.Regex
import scala.concurrent.ExecutionContext.Implicits.global


object Exercice1 {

  var LIEN_MONSTRE_REGEX = "(?<=<li><a href=\").+?(?=\")"
  var LIEN_SPELL_REGEX  ="(?<=\\/spells\\/).+?(?=\")"
  var NOM_CREATURE_REGEX ="(?<=\">).+?(?=<\\/h1)"

  val creatures = new ArrayBuffer[Creature]
  var PATTERN_LIEN_MONSTRE = new Regex(LIEN_SPELL_REGEX)

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("crawler").setMaster("local[*]")
    val sc = new SparkContext(conf)

    if(!new File("results").exists()){
      crawlMonsters("http://legacy.aonprd.com/bestiary/","monsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary2/","additionalMonsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary3/","monsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary4/","monsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary5/","index.html")

      val rdd = sc.parallelize(creatures)
      rdd.saveAsObjectFile("results")

    }
      val rdd = sc.objectFile[Creature]("results")
      val map = rdd.map(creature=> creature.spells.map(spell=>(spell, creature.name))).flatMap(x=>x).reduceByKey((k,v)=>(k+v))


      map.foreach(println)

  }

  private def crawlMonsters(src : String, index : String) = {
    val html = Source.fromURL(src+index).mkString
    val htmlLiensMonstre = html.substring(html.indexOf("monster-index-wrapper"), html.indexOf("footer"))
    val pattern = new Regex(LIEN_MONSTRE_REGEX)
    val iterator = pattern findAllIn (htmlLiensMonstre)
    while (iterator.hasNext) {
      val lien = src + iterator.next()
      crawlMonstre(lien)
    }

  }

  def crawlMonstre(monstreUrl : String): Unit ={
    val html = Source.fromURL(monstreUrl).mkString
    val htmlInfosMontre = html.substring(html.indexOf("nav-path"),html.indexOf("footer"))
    val monstres = htmlInfosMontre.split("<h1 ")
    for(monstre <- monstres){
      val nomCreature = new Regex(NOM_CREATURE_REGEX).findFirstIn(monstre).mkString("")
      val creature = new Creature(nomCreature)
      val iterator = PATTERN_LIEN_MONSTRE findAllIn(monstre)
      while(iterator.hasNext){
        val infos = iterator.next()
        val spellInfos = infos.split("#")
        if(spellInfos.length==2){
          creature.addSpell(spellInfos(1))
        } else{
          creature.addSpell(spellInfos(0).replace(".html", "")) // On supprime ".html si on a pas le nom du sort. Certains sorts n'ont pas le bon format
        }
      }
      if(!nomCreature.isEmpty && !creature.spells.isEmpty)
        creatures+=creature
        println(creature.name)
    }
  }
}
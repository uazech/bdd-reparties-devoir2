package exercice1

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.matching.Regex

object Excercice1Bonus {

  var LIEN_MONSTRE_REGEX = "(?<=<li><a href=\").+?(?=\")"
  var LIEN_SPELL_REGEX = "(?<=\\/spells\\/).+?(?=\")"
  var ROOT_LIEN_SPELL = "(\\/)[A-Za-z1-9]{1,}(\\/spells\\/"
  var NOM_CREATURE_REGEX = "(?<=\">).+?(?=<\\/h1)"
  var SPELL_RESISTANCE_YES_REGEX = ".*Spell Resistance</a></b> yes.*"

  val creatures = new ArrayBuffer[CreatureBO]
  val spells = new ArrayBuffer[Spell]
  var PATTERN_LIEN_MONSTRE = new Regex(LIEN_SPELL_REGEX)

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("crawler").setMaster("local[*]")
    val sc = new SparkContext(conf)

    if (!new File("results").exists() &&  !new File("spells").exists() ) {
      crawlMonsters("http://legacy.aonprd.com/bestiary/", "monsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary2/","additionalMonsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary3/","monsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary4/","monsterIndex.html")
      crawlMonsters("http://legacy.aonprd.com/bestiary5/","index.html")

      val rdd = sc.parallelize(creatures)
      rdd.saveAsObjectFile("results")

      val rddSpells = sc.parallelize(spells)
      rddSpells.saveAsObjectFile("spells")
    }


    val rdd = sc.objectFile[CreatureBO]("results")
    val rddSpells = sc.objectFile[Spell]("spells")
    val map = rdd.map(creature => creature.spells.map(spell => (spell, creature.name))).flatMap(x => x).reduceByKey((k, v) => (k + "," + v))
    println("Liste spells ")
    map.foreach(println)
    val mapSpell = rddSpells.map(spell => (spell.name, spell.resistance)).reduceByKey((k, p) => (p && k))
    println(" bonus: Liste spells resistance = false ")
    val sansResistance = mapSpell.join(map).filter(spells => spells._2._1 == false).map(spells => (spells._1, spells._2._2))
    sansResistance.foreach(println)
  }

  private def crawlMonsters(src: String, index: String) = {
    val html = Source.fromURL(src + index).mkString
    val htmlLiensMonstre = html.substring(html.indexOf("monster-index-wrapper"), html.indexOf("footer"))
    val pattern = new Regex(LIEN_MONSTRE_REGEX)
    val iterator = pattern findAllIn (htmlLiensMonstre)

    while (iterator.hasNext) {
      val lien = src + iterator.next()
      crawlMonstre(lien)
    }
  }

  def crawlMonstre(monstreUrl: String): Unit = {
    val html = Source.fromURL(monstreUrl).mkString
    val htmlInfosMontre = html.substring(html.indexOf("nav-path"), html.indexOf("footer"))
    val monstres = htmlInfosMontre.split("<h1 ")
    for (monstre <- monstres) {
      val nomCreature = new Regex(NOM_CREATURE_REGEX).findFirstIn(monstre).mkString("")
      val creature = new CreatureBO(nomCreature)
      val iterator = PATTERN_LIEN_MONSTRE findAllIn (monstre)

      var nomSpell = ""
      while (iterator.hasNext){
        val infos = iterator.next()
        val spellInfos = infos.split("#")
        if (spellInfos.length == 2) {
          nomSpell = spellInfos(1)
          creature.addSpell(nomSpell)
        } else {
          nomSpell = spellInfos(0).replace(".html", "")
          creature.addSpell(nomSpell) // On supprime ".html si on a pas le nom du sort. Certains sorts n'ont pas le bon formatrtains sorts n'ont pas le bon format
        }
        val root_lien_spell = new Regex(ROOT_LIEN_SPELL + infos + ")").findFirstIn(monstre).mkString
        crawlSpells("http://legacy.aonprd.com", root_lien_spell, nomSpell)
      }
      if (!nomCreature.isEmpty && !creature.spells.isEmpty)
      {
        creatures += creature
        println(creature.name)
      }
    }
  }

  private def crawlSpells(src: String, index: String, nomSpell: String) = {
    val html = Source.fromURL(src + index).mkString
    val pattern = new Regex(SPELL_RESISTANCE_YES_REGEX)
    val result = pattern.findFirstIn(html).getOrElse("no match")

    if (result == "no match") {
      spells += new Spell(nomSpell, false)
    }
    else
      spells += new Spell(nomSpell, true)
  }
}

package ex2

import org.apache.spark.graphx.{Edge, Graph}
import org.apache.spark.{SparkConf, SparkContext}
;
object SimpleExample extends App {
//from https://blog.knoldus.com/aggregating-neighboring-vertices-with-apache-spark-graphx-library/
  val conf = new SparkConf()
    .setAppName("Simulation 1")
    .setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  var users = sc.parallelize(Array((1L, 17), (2L, 19), (3L, 27), (4L, 13), (5L, 25), (6L, 32), (7L, 35), (8L, 29), (9L, 13)))
  var follows = sc.parallelize(Array(Edge(1L, 3L, "follow"), Edge(2L, 3L, "follow"), Edge(2L, 4L, "follow"), Edge(4L, 5L, "follow"), Edge(4L, 7L, "follow"),
    Edge(5L, 2L, "follow"), Edge(6L, 7L, "follow"), Edge(6L, 4L, "follow"), Edge(9L, 8L, "follow"), Edge(9L, 1L, "follow"), Edge(9L, 3L, "follow")))

  val twitterGraph = Graph(users, follows)

  val followersUnderTwenty = twitterGraph.aggregateMessages[Int] ( tripletFields=> { if (tripletFields.srcAttr <20) tripletFields.sendToDst(1)},(a, b) => (a + b))

println(  followersUnderTwenty.reduce((a,b)=>if(a._2>b._2) a else b))

}

package ex2

import java.nio.file.Files
import java.nio.file.Paths
import java.io._

object Util {
  def calculerNroAleatoire(a: Int, b: Int): Int = {
    return a + (Math.random * ((a - b) + 1)).asInstanceOf[Int]
  }


  def calculeDistance(x: Float, y: Float, x1: Float, y1: Float): Float = {
    return (Math.sqrt(Math.pow((x1 - x), 2) + Math.pow((y1 - y), 2))).toFloat
    // return 4F
  }

  def makeFolder(nom: String) = {
    if (!Files.exists(Paths.get(nom)))
      Files.createDirectory(Paths.get(nom))
    else
      cleanDir(new File(nom))
  }

  def cleanDir(dir: File): Int = {
    def loop(list: Array[File], deletedFiles: Int): Int = {
      if (list.isEmpty) deletedFiles
      else {
        if (list.head.isDirectory && !list.head.listFiles().isEmpty) {
          loop(list.head.listFiles() ++ list.tail ++ Array(list.head), deletedFiles)
        } else {
          val isDeleted = list.head.delete()
          if (isDeleted) loop(list.tail, deletedFiles + 1)
          else loop(list.tail, deletedFiles)
        }
      }
    }

    loop(dir.listFiles(), 0)
  }


}

package ex2

import java.awt.image.BufferedImage
import java.awt.{Graphics2D, Color, Font, BasicStroke}
import java.awt.geom._

class DrawImage(title: String) {

  val size = (600, 600)
  val canvas = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB)
  val g = canvas.createGraphics()
  g.scale(1.20, 1.20)
  var vertices = Array.empty[(Long, Creature)]

  def init(): Unit = {
    g.translate(350, 350)
    g.setColor(Color.WHITE)
    g.fillRect(-350, -350, canvas.getWidth, canvas.getHeight)
    g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
      java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
  }


  def draw(nroIteration: Int): Unit = {
    var legend = Map.empty[(String), (Tuple3[Tuple3[Int, Int, Int], Int, Int])]
    vertices.foreach(arg => {
      configurePoint("", new Color(arg._2.color._1, arg._2.color._2, arg._2.color._3), arg._2.x, arg._2.y)
      var count = legend.find(_._1.equals(arg._2.nom)).getOrElse("", ((0, 0, 0), 0, 0))
      legend += (arg._2.nom -> ((arg._2.color._1, arg._2.color._2, arg._2.color._3), count._2._2 + 1, arg._2.equipe))
    })

    var x = -300
    legend.foreach(arg => {
      configurePoint("", new Color(arg._2._1._1, arg._2._1._2, arg._2._1._3), x, 220)
      configureLabel(arg._1, x + 10, 220, 10)
      configureLabel("Cont:" + arg._2._2.toString + " Eq:" + arg._2._3, x + 12, 230, 9)
      x += arg._1.length + 65
    })

    g.setColor(new Color(0, 0, 255)) // same as Color.BLUE
    g.draw(new Line2D.Double(-300, 210, 600, 210))
    configureLabel(title, -250, -300, 15)
    g.dispose()
    val folder = title.filterNot(_ == ' ')
    javax.imageio.ImageIO.write(canvas, "png", new java.io.File(s"${folder}/iteration_${nroIteration}.png"))
  }

  def configurePoint(label: String, color: Color, x: Double, y: Double): Unit = {
    g.setColor(color)
    g.fill(new Ellipse2D.Double(x, y, 10.0, 10.0))
    val xLabel = if ((x - label.length) > 0) x - label.length else 0
    configureLabel(label, xLabel, y + 15, 10)
  }

  def configureLabel(label: String, x: Double, y: Double, size: Int): Unit = {
    g.setColor(Color.BLACK) // a darker green
    g.setFont(new Font("Batang", Font.PLAIN, size))
    g.drawString(label, x.toFloat, y.toFloat)
  }

}

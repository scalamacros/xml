package org.scalamacros.xml

import scala.reflect.api.Universe

trait Expression {
  val u: Universe; import u._

  case class Expression(tree: Tree) extends xml.SpecialNode {
    def label: String = s"{${showCode(tree)}}"
    def buildString(sb: StringBuilder): StringBuilder = sb.append(label)
  }
}

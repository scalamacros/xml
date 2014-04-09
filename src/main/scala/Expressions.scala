package org.scalamacros.xml

import scala.reflect.api.Universe

trait Expressions {
  protected val u: Universe; import u._

  case class Expression(tree: Tree) extends xml.SpecialNode {
    def label: String = "#EXPR"
    def buildString(sb: StringBuilder): StringBuilder = sb.append(s"{${showCode(tree)}}")
  }
}

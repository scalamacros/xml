package org.scalamacros.xml

import scala.reflect.api.Universe

trait Nodes {
  protected val u: Universe; import u._

  case class Unquote(tree: Tree) extends xml.SpecialNode {
    def label: String = "#UNQUOTE"
    def buildString(sb: StringBuilder): StringBuilder = sb.append(s"{${showCode(tree)}}")
  }
}

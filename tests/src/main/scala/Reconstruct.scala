import reflect.macros.Context
import language.experimental.macros

import org.scalamacros.xml.MacroLiftables

object Reconstruct {
  def apply[N <: xml.Node](node: N): N = macro impl[N]
  def impl[N <: xml.Node](c: Context)(node: c.Expr[N]): c.Expr[N] = {
    val bundle = new Bundle[c.type](c)
    c.Expr[N](bundle.expand(node.tree))
  }
}

class Bundle[C <: Context with Singleton](val c: C) extends MacroLiftables {
  import c.universe._
  def expand(node: Tree): Tree = {
    val q"${unlifted: xml.Node}" = node
    val liftedBack = q"$unlifted"
    liftedBack
  }
}
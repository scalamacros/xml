import reflect.macros.blackbox.Context
import language.experimental.macros

import org.scalamacros.xml.MacroLiftables

object Reconstruct {
  def apply[N <: xml.Node](node: N): N = macro impl.expand
  class impl(val c: Context) extends MacroLiftables { import c.universe._
    def expand(node: Tree): Tree = {
      val q"${unlifted: xml.Node}" = node
      val liftedBack = q"$unlifted"
      liftedBack
    }
  }
}

package org.scalamacros.xml

import reflect.macros.blackbox.Context

trait MacroLiftables extends Liftables with Unliftables with Nodes {
  val c: Context
  protected lazy val u: c.universe.type = c.universe
}


package org.scalamacros.xml

import reflect.macros.Context

trait MacroLiftables extends Liftables with Unliftables with Nodes {
  val c: Context
  protected lazy val __universe: c.universe.type = c.universe
}


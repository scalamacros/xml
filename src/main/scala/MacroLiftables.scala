package org.scalamacros.xml

import reflect.macros.blackbox.Context
import reflect.runtime.universe

trait MacroLiftables extends Liftables with Unliftables with Expressions {
  val c: Context
  protected lazy val u: c.universe.type = c.universe
}


package org.scalamacros.xml

import reflect.runtime.universe

object RuntimeLiftables extends Liftables with Unliftables with Expressions {
  protected lazy val u: universe.type = universe
}


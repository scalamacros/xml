package org.scalamacros.xml

import reflect.runtime.universe

object runtime extends Liftables with Unliftables {
  lazy val u: universe.type = universe
}


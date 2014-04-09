import org.scalatest.FunSuite
import reflect.runtime.universe._
import org.scalamacros.xml.RuntimeLiftables._

class ReconstructSuite extends FunSuite {
  // test("reconstruct comment") {
  //   assert(Reconstruct(<!--foo-->) == <!--foo-->)
  // }

  // test("reconstruct text") {
  //   assert(Reconstruct(<![CDATA[foo]]>) == <![CDATA[foo]]>)
  // }

  // test("reconstruct entity ref") {
  //   assert(Reconstruct(<foo>&amp;</foo>) == <foo>&amp;</foo>)
  // }

  // test("reconstruct proc instr") {
  //   assert(Reconstruct(<foo><?foo bar?></foo>) == <foo><?foo bar?></foo>)
  // }

  // test("reconstruct unparsed") {
  //   assert(Reconstruct(<xml:unparsed>foo</xml:unparsed>) == <xml:unparsed>foo</xml:unparsed>)
  // }

  // test("reconstruct minimized elem") {
  //   assert(Reconstruct(<foo/>) == <foo/>)
  // }

  // test("reconstruct maximized elem") {
  //   assert(Reconstruct(<foo></foo>) == <foo></foo>)
  // }

  // test("reconstruct prefixed elem") {
  //   assert(Reconstruct(<foo:bar/>) == <foo:bar/>)
  // }

  // test("reconstruct nested elem") {
  //   assert(Reconstruct(<foo><bar/></foo>) == <foo><bar/></foo>)
  // }

  // test("reconstruct elem with unprefixed attributes") {
  //   assert(Reconstruct(<foo a="a" b="b"/>) == <foo a="a" b="b"/>)
  // }

  // test("reconstruct elem with prefixed attributes") {
  //   assert(Reconstruct(<foo a:a="a" b:b="b"/>) == <foo a:a="a" b:b="b"/>)
  // }

  // test("reconstruct unquote within elem") {
  //   assert(Reconstruct(<foo>{2 + 3}</foo>) == <foo>{2 + 3}</foo>)
  // }

  // test("reconstruct unquote within unprefixed attribute") {
  //   assert(Reconstruct(<foo a={"foo" + "bar"}/>) == <foo a={"foo" + "bar"}/>)
  // }

  // test("reconstruct unquote within prefixed attribute") {
  //   assert(Reconstruct(<foo a:b={"foo" + "bar"}/>) == <foo a:b={"foo" + "bar"}/>)
  // }

  // test("reconstruct namespaced elem") {
  //   assert(Reconstruct(<foo xmlns:pre="uri"/>) == <foo xmlns:pre="uri"/>)
  // }

  // test("reconstruct multi-namespaced elem") {
  //   assert(Reconstruct(<foo xmlns:a="uri1" xmlns:b="uri2"/>) == <foo xmlns:a="uri1" xmlns:b="uri2"/>)
  // }

  // test("reconstruct nested namespaced elem") {
  //   assert(Reconstruct(<foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>) == <foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>)
  // }

  // test("reconstruct shadowed namespaced elem") {
  //   assert(Reconstruct(<foo xmlns:pre="a"><bar xmlns:pre="b"/></foo>) == <foo xmlns:pre="a"><bar xmlns:pre="b"/></foo>)
  // }
}

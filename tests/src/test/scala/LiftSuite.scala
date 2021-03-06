import org.scalatest.FunSuite
import reflect.runtime.universe._
import org.scalamacros.xml.RuntimeLiftables._

class LiftSuite extends FunSuite {
  test("lift comment") {
    assert(q"${xml.Comment("foo")}" equalsStructure q"<!--foo-->")
  }

  test("lift text") {
    assert(q"${xml.Text("foo")}" equalsStructure q"<![CDATA[foo]]>")
  }

  test("lift entity ref") {
    assert(q"<foo>{${xml.EntityRef("amp")}}</foo>" equalsStructure q"<foo>&amp;</foo>")
  }

  test("lift proc instr") {
    assert(q"${xml.ProcInstr("foo", "bar")}" equalsStructure q"<?foo bar?>")
  }

  test("lift unparsed") {
    assert(q"${xml.Unparsed("foo")}" equalsStructure q"<xml:unparsed>foo</xml:unparsed>")
  }

  test("lift minimized elem") {
    assert(q"${<foo/>}" equalsStructure q"<foo/>")
  }

  test("lift maximized elem") {
    assert(q"${<foo></foo>}" equalsStructure q"<foo></foo>")
  }

  test("lift prefixed elem") {
    assert(q"${<foo:bar/>}" equalsStructure q"<foo:bar/>")
  }

  test("lift nested elem") {
    assert(q"${<foo><bar/></foo>}" equalsStructure q"<foo><bar/></foo>")
  }

  test("lift elem with unprefixed attributes") {
    assert(q"${<foo a="a" b="b"/>}" equalsStructure q"""<foo a="a" b="b"/>""")
  }

  test("lift elem with prefixed attributes") {
    assert(q"${<foo a:a="a" b:b="b"/>}" equalsStructure q"""<foo a:a="a" b:b="b"/>""")
  }

  test("lift unquote within elem") {
    assert(q"${<foo>{Unquote(q"x + y")}</foo>}" equalsStructure q"<foo>{x + y}</foo>")
  }

  test("lift unquote within unprefixed attribute") {
    assert(q"${<foo a={Unquote(q"x + y")}/>}" equalsStructure q"<foo a={x + y}/>")
  }

  test("lift unquote within prefixed attribute") {
    assert(q"${<foo a:b={Unquote(q"x + y")}/>}" equalsStructure q"<foo a:b={x + y}/>")
  }

  test("lift namespaced elem") {
    assert(q"${<foo xmlns:pre="uri"/>}" equalsStructure q"""<foo xmlns:pre="uri"/>""")
  }

  test("lift multi-namespaced elem") {
    assert(q"${<foo xmlns:a="uri1" xmlns:b="uri2"/>}" equalsStructure q"""<foo xmlns:a="uri1" xmlns:b="uri2"/>""")
  }

  test("lift nested namespaced elem") {
    assert(q"${<foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>}"
           equalsStructure
           q"""<foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>""")
  }

  test("lift shadowed namespaced elem") {
    assert(q"${<foo xmlns:pre="a"><bar xmlns:pre="b"/></foo>}"
           equalsStructure
           q"""<foo xmlns:pre="a"><bar xmlns:pre="b"/></foo>""")
  }
}

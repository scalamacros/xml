import org.scalatest.FunSuite
import reflect.runtime.universe._
import org.scalamacros.xml.RuntimeLiftables._

class UnliftSuite extends FunSuite {
  test("unlift comment") {
    val q"${comment: xml.Comment}" = q"<!--foo-->"
    assert(comment.commentText == "foo")
  }

  test("unlift text") {
    val q"${text: xml.Text}" = q"<![CDATA[foo]]>"
    assert(text.text == "foo")
  }

  test("unlift entity ref") {
    val q"<foo>{${eref: xml.EntityRef}}</foo>" = q"<foo>&amp;</foo>"
    assert(eref.entityName == "amp")
  }

  test("unlift proc instr") {
    val q"${pi: xml.ProcInstr}" = q"<?foo bar?>"
    assert(pi.target == "foo" && pi.proctext == "bar")
  }

  test("unlift unparsed") {
    val q"${unparsed: xml.Unparsed}" = q"<xml:unparsed>foo</xml:unparsed>"
    assert(unparsed.data == "foo")
  }

  test("unlift minimized elem") {
    val q"${elem: xml.Elem}" = q"<foo/>"
    val <foo/> = elem
  }

  test("unlift maximized elem") {
    val q"${elem: xml.Elem}" = q"<foo></foo>"
    val <foo></foo> = elem
  }

  test("unlift prefixed elem") {
    val q"${elem: xml.Elem}" = q"<foo:bar/>"
    val <foo:bar/> = elem
  }

  test("unlift nested elem") {
    val q"${elem: xml.Elem}" = q"<foo><bar/></foo>"
    val <foo><bar/></foo> = elem
  }

  test("unlift elem with unprefixed attribute") {
    val q"${elem: xml.Elem}" = q"""<foo x="y"/>"""
    val xml.UnprefixedAttribute("x", xml.Text("y"), xml.Null) = elem.attributes
  }

  test("unlift elem with prefixed attribute") {
    val q"${elem: xml.Elem}" = q"""<foo a:b="c"/>"""
    val xml.PrefixedAttribute("a", "b", xml.Text("c"), xml.Null) = elem.attributes
  }

  test("unlift expression within elem") {
    val q"${elem: xml.Elem}" = q"<foo>{x + y}</foo>"
    val <foo>{Expression(q"x + y")}</foo> = elem
  }

  test("unlift expression within unprefixed attribute") {
    val q"${elem: xml.Elem}" = q"<foo a={x + y}/>"
    val xml.UnprefixedAttribute("a", Expression(q"x + y"), xml.Null) = elem.attributes
  }

  test("unlift expression within prefixed attribute") {
    val q"${elem: xml.Elem}" = q"<foo a:b={x + y}/>"
    val xml.PrefixedAttribute("a", "b", Expression(q"x + y"), xml.Null) = elem.attributes
  }

  test("unlift namespaced elem") {
    val q"${foo: xml.Elem}" = q"""<foo xmlns:pre="uri"/>"""
    val xml.NamespaceBinding("pre", "uri", xml.TopScope) = foo.scope
  }

  test("unlift nested namespaced elem") {
    val q"${foo: xml.Elem}" = q"""<foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>"""
    val xml.NamespaceBinding("pre1", "uri1", xml.TopScope) = foo.scope
    val <foo>{bar: xml.Elem}</foo> = foo
    val xml.NamespaceBinding("pre2", "uri2", xml.NamespaceBinding("pre1", "uri1", xml.TopScope)) = bar.scope
  }
}

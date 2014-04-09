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

  test("unlift unquote within elem") {
    val q"${elem: xml.Elem}" = q"<foo>{x + y}</foo>"
    val <foo>{Unquote(q"x + y")}</foo> = elem
  }

  test("unlift unquote within unprefixed attribute") {
    val q"${elem: xml.Elem}" = q"<foo a={x + y}/>"
    val xml.UnprefixedAttribute("a", Unquote(q"x + y"), xml.Null) = elem.attributes
  }

  test("unlift unquote within prefixed attribute") {
    val q"${elem: xml.Elem}" = q"<foo a:b={x + y}/>"
    val xml.PrefixedAttribute("a", "b", Unquote(q"x + y"), xml.Null) = elem.attributes
  }

  test("unlift namespaced elem") {
    val q"${foo: xml.Elem}" = q"""<foo xmlns:pre="uri"/>"""
    val xml.NamespaceBinding("pre", "uri", xml.TopScope) = foo.scope
  }

  test("unlift multi-namespaced elem") {
    val q"${foo: xml.Elem}" = q"""<foo xmlns:a="uri1" xmlns:b="uri2"/>"""
    val xml.NamespaceBinding("b", "uri2", xml.NamespaceBinding("a", "uri1", xml.TopScope)) = foo.scope
  }

  test("unlift nested namespaced elem") {
    val q"${foo: xml.Elem}" = q"""<foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>"""
    val xml.NamespaceBinding("pre1", "uri1", xml.TopScope) = foo.scope
    val <foo>{bar: xml.Elem}</foo> = foo
    val xml.NamespaceBinding("pre2", "uri2", ns @ xml.NamespaceBinding("pre1", "uri1", xml.TopScope)) = bar.scope
    assert(foo.scope eq ns)
  }

  test("unlift shadowed namespaced elem") {
    val q"${foo: xml.Elem}" = q"""<foo xmlns:pre="a"><bar xmlns:pre="b"/></foo>"""
    val xml.NamespaceBinding("pre", "a", xml.TopScope) = foo.scope
    val <foo>{bar: xml.Elem}</foo> = foo
    val xml.NamespaceBinding("pre", "b", ns @ xml.NamespaceBinding("pre", "a", xml.TopScope)) = bar.scope
    assert(ns eq foo.scope)
  }
}

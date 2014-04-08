package org.scalamacros.xml

import scala.reflect.api.Universe

trait Unliftables extends Expression {
  val u: Universe; import u._

  implicit val UnliftComment = Unliftable[xml.Comment] {
    case q"new _root_.scala.xml.Comment(${text: String})" => xml.Comment(text)
  }

  implicit val UnliftText = Unliftable[xml.Text] {
    case q"new _root_.scala.xml.Text(${text: String})" => xml.Text(text)
  }

  implicit val UnliftEntityRef = Unliftable[xml.EntityRef] {
    case q"new _root_.scala.xml.EntityRef(${name: String})" => xml.EntityRef(name)
  }

  implicit val UnliftProcInstr = Unliftable[xml.ProcInstr] {
    case q"new _root_.scala.xml.ProcInstr(${target: String}, ${proctext: String})" =>
      xml.ProcInstr(target, proctext)
  }

  implicit val UnliftUnparsed = Unliftable[xml.Unparsed] {
    case q"new _root_.scala.xml.Unparsed(${data: String})" => xml.Unparsed(data)
  }

  implicit val UnliftPCData = Unliftable[xml.PCData] {
    case q"new _root_.scala.xml.PCData(${data: String})" => xml.PCData(data)
  }

  // extract string literal or null
  private object Str {
    def unapply(tree: Tree): Option[String] = tree match {
      case Literal(Constant(s: String)) => Some(s)
      case Literal(Constant(null))      => Some(null)
      case _                            => None
    }
  }

  // extract a sequence of $md = FooAttribute(..., $md) as metadata
  private object Attributes {
    def unapply(attributes: List[Tree]): Option[xml.MetaData] =
      try Some(attributes.foldLeft[xml.MetaData](xml.Null) {
        case (md, q"$$md = new _root_.scala.xml.UnprefixedAttribute(${key: String}, ${value: xml.Node}, $$md)") =>
          new xml.UnprefixedAttribute(key, value, md)
        case (md, q"$$md = new _root_.scala.xml.UnprefixedAttribute(${key: String}, $expr, $$md)") =>
          new xml.UnprefixedAttribute(key, Expression(expr), md)
        case (md, q"$$md = new _root_.scala.xml.PrefixedAttribute(${pre: String}, ${key: String}, ${value: xml.Node}, $$md)") =>
          new xml.PrefixedAttribute(pre, key, value, md)
        case (md, q"$$md = new _root_.scala.xml.PrefixedAttribute(${pre: String}, ${key: String}, $expr, $$md)") =>
          new xml.PrefixedAttribute(pre, key, Expression(expr), md)
      }) catch {
        case _: MatchError => None
      }
  }

  // extract a seq of nodes from mutable nodebuffer-based construction
  private object Children {
    def unapply(children: List[Tree]): Option[Seq[xml.Node]] = children match {
      case Nil => Some(Nil)
      case q"{ val $$buf = new _root_.scala.xml.NodeBuffer; ..$additions; $$buf }: _*" :: Nil =>
        try Some(additions.map {
          case q"$$buf &+ ${node: xml.Node}" => node
          case q"$$buf &+ $expr"             => Expression(expr)
        }) catch {
          case _: MatchError => None
        }
      case _ => None
    }
  }

  implicit val UnliftElem: Unliftable[xml.Elem] = Unliftable[xml.Elem] {
    case q"new _root_.scala.xml.Elem(${Str(prefix)}, ${Str(label)}, _root_.scala.xml.Null, $_, ${minimizeEmpty: Boolean}, ..${Children(children)})" =>
      xml.Elem(prefix, label, _root_.scala.xml.Null, xml.TopScope, minimizeEmpty, children: _*)
    case q"""
           var $$md: _root_.scala.xml.MetaData = _root_.scala.xml.Null
           ..${Attributes(attrs)}
           new _root_.scala.xml.Elem(${Str(prefix)}, ${Str(label)}, $$md, $_, ${minimizeEmpty: Boolean}, ..${Children(children)})
         """ =>
      xml.Elem(prefix, label, attrs, xml.TopScope, minimizeEmpty, children: _*)
  }

  implicit val UnliftAtom = Unliftable[xml.Atom[String]] {
    case UnliftPCData(pcdata)     => pcdata
    case UnliftText(text)         => text
    case UnliftUnparsed(unparsed) => unparsed
  }

  implicit val UnliftSpecialNode: Unliftable[xml.SpecialNode] = Unliftable[xml.SpecialNode] {
    case UnliftAtom(atom)           => atom
    case UnliftComment(comment)     => comment
    case UnliftProcInstr(procinstr) => procinstr
    case UnliftEntityRef(entityref) => entityref
  }

  implicit val UnliftNode: Unliftable[xml.Node] = Unliftable[xml.Node] {
    case UnliftElem(elem)         => elem
    case UnliftSpecialNode(snode) => snode
  }
}

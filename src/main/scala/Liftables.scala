package org.scalamacros.xml

import scala.reflect.api.Universe

trait Liftables extends Expressions {
  protected val u: Universe; import u._

  implicit val liftComment = Liftable[xml.Comment] { c =>
    q"new _root_.scala.xml.Comment(${c.commentText})"
  }

  implicit val liftText = Liftable[xml.Text] { t =>
    q"new _root_.scala.xml.Text(${t.text})"
  }

  implicit val liftEntityRef = Liftable[xml.EntityRef] { er =>
    q"new _root_.scala.xml.EntityRef(${er.entityName})"
  }

  implicit val liftExpression = Liftable[Expression] { _.tree }

  implicit val liftProcInstr = Liftable[xml.ProcInstr] { pi =>
    q"new _root_.scala.xml.ProcInstr(${pi.target}, ${pi.proctext})"
  }

  implicit val liftUnparsed = Liftable[xml.Unparsed] { u =>
    q"new _root_.scala.xml.Unparsed(${u.data})"
  }

  implicit val liftPCData = Liftable[xml.PCData] { pcd =>
    q"new _root_.scala.xml.PCData(${pcd.data})"
  }

  implicit val liftElem = Liftable[xml.Elem] { elem =>
    def liftMeta(meta: xml.MetaData): List[Tree] = meta match {
      case xml.Null =>
        q"var $$md: _root_.scala.xml.MetaData = _root_.scala.xml.Null" :: Nil
      case xml.UnprefixedAttribute(key, Seq(value), rest) =>
        q"$$md = new _root_.scala.xml.UnprefixedAttribute($key, $value, $$md)" :: liftMeta(rest)
      case xml.PrefixedAttribute(pre, key, Seq(value), rest) =>
        q"$$md = new _root_.scala.xml.PrefixedAttribute($pre, $key, $value, $$md)" :: liftMeta(rest)
    }

    val (metapre, metaval) =
      if (elem.attributes.isEmpty) (Nil, q"_root_.scala.xml.Null")
      else (liftMeta(elem.attributes).reverse, q"$$md")

    val children =
      if (elem.child.isEmpty) q""
      else {
        val additions = elem.child.map { node => q"$$buf &+ $node" }
        q"""{
          val $$buf = new _root_.scala.xml.NodeBuffer
          ..$additions
          $$buf
        }: _*"""
      }

    // TODO: find out true meaning and correct usage of $scope
    q"""
      ..$metapre
      new _root_.scala.xml.Elem(${elem.prefix}, ${elem.label}, $metaval, $$scope, ${elem.minimizeEmpty}, ..$children)
    """
  }

  // TODO: what to do with Atom[T]?
  implicit val liftAtom = Liftable[xml.Atom[String]] {
    case pcdata:   xml.PCData   => liftPCData(pcdata)
    case text:     xml.Text     => liftText(text)
    case unparsed: xml.Unparsed => liftUnparsed(unparsed)
  }

  implicit val liftSpecialNode = Liftable[xml.SpecialNode] {
    case atom:      xml.Atom[String] => liftAtom(atom)
    case comment:   xml.Comment      => liftComment(comment)
    case procinstr: xml.ProcInstr    => liftProcInstr(procinstr)
    case entityref: xml.EntityRef    => liftEntityRef(entityref)
    case expr:      Expression       => liftExpression(expr)
  }

  implicit val liftNode: Liftable[xml.Node] = Liftable[xml.Node] {
    case elem:  xml.Elem         => liftElem(elem)
    case snode: xml.SpecialNode  => liftSpecialNode(snode)
  }
}

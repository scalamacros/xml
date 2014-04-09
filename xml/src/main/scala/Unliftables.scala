package org.scalamacros.xml

import scala.reflect.api.Universe

trait Unliftables extends Nodes {
  protected val __universe: Universe
  import __universe._
  import __universe.internal.reificationSupport.{SyntacticBlock => SynBlock}

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

  private def withRetreat[T](f: (() => Nothing) => T)(orElse: => T): T = {
    class Stop extends Exception
    try f(() => throw new Stop) catch {
      case _: Stop => orElse
    }
  }

  private object Scoped {
    def unapply(tree: Tree)(implicit outer: xml.NamespaceBinding): Option[(xml.NamespaceBinding, Tree)] = tree match {
      case q"""
             var $$tmpscope: _root_.scala.xml.NamespaceBinding = $$scope
             ..$scopes
             ${SynBlock(q"val $$scope: _root_.scala.xml.NamespaceBinding = $$tmpscope" :: last)}
           """ =>
        withRetreat { retreat =>
          Some((scopes.foldLeft[xml.NamespaceBinding](outer) {
            case (ns, q"$$tmpscope = new _root_.scala.xml.NamespaceBinding(${Str(prefix)}, ${uri: String}, $$tmpscope)") =>
              xml.NamespaceBinding(prefix, uri, ns)
            case _ =>
              retreat()
          }, q"..$last"))
        } {
          Some((outer, tree))
        }
      case _ =>
        Some((outer, tree))
    }
  }

  // extract a sequence of $md = FooAttribute(..., $md) as metadata
  private object Attributed {
    def unapply(tree: Tree)(implicit outer: xml.NamespaceBinding): Option[(xml.MetaData, Tree)] = tree match {
      case q"""
             var $$md: _root_.scala.xml.MetaData = _root_.scala.xml.Null
             ..$attributes
             $last
            """ =>
        withRetreat { retreat =>
          Some((attributes.foldLeft[xml.MetaData](xml.Null) {
            case (md, q"$$md = new _root_.scala.xml.UnprefixedAttribute(${key: String}, ${value: xml.Node}, $$md)") =>
              new xml.UnprefixedAttribute(key, value, md)
            case (md, q"$$md = new _root_.scala.xml.UnprefixedAttribute(${key: String}, $expr, $$md)") =>
              new xml.UnprefixedAttribute(key, Unquote(expr), md)
            case (md, q"$$md = new _root_.scala.xml.PrefixedAttribute(${pre: String}, ${key: String}, ${value: xml.Node}, $$md)") =>
              new xml.PrefixedAttribute(pre, key, value, md)
            case (md, q"$$md = new _root_.scala.xml.PrefixedAttribute(${pre: String}, ${key: String}, $expr, $$md)") =>
              new xml.PrefixedAttribute(pre, key, Unquote(expr), md)
            case _ =>
              retreat()
          }, last))
        } {
          Some((xml.Null, tree))
        }
      case _ => Some((xml.Null, tree))
    }
  }

  // extract a seq of nodes from mutable nodebuffer-based construction
  private object Children {
    def unapply(children: List[Tree])(implicit outer: xml.NamespaceBinding): Option[Seq[xml.Node]] = children match {
      case Nil => Some(Nil)
      case q"{ val $$buf = new _root_.scala.xml.NodeBuffer; ..$additions; $$buf }: _*" :: Nil =>
        try Some(additions.map {
          case q"$$buf &+ ${node: xml.Node}" => node
          case q"$$buf &+ $unquote"          => Unquote(unquote)
        }) catch {
          case _: MatchError => None
        }
      case _ => None
    }
  }

  private def correspondsAttrRef(attrs: xml.MetaData, attrref: Tree): Boolean = (attrs, attrref) match {
    case (xml.Null, q"_root_.scala.xml.Null")     => true
    case (metadata, q"$$md") if metadata.nonEmpty => true
    case _                                        => false
  }

  implicit def UnliftElem(implicit outer: xml.NamespaceBinding = xml.TopScope): Unliftable[xml.Elem] = new Unliftable[xml.Elem] {
    def unapply(tree: Tree): Option[xml.Elem] = {
      val Scoped(scope, inner) = tree;
      {
        val outer = 'shadowed
        implicit val current = scope
        inner match {
          case Attributed(attrs,
                 q"new _root_.scala.xml.Elem(${Str(prefix)}, ${Str(label)}, $attrref, $$scope, ${minimizeEmpty: Boolean}, ..${Children(children)})")
               if correspondsAttrRef(attrs, attrref) =>
            Some(xml.Elem(prefix, label, attrs, scope, minimizeEmpty, children: _*))
          case _ =>
            None
        }
      }
    }
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

  implicit def UnliftNode(implicit outer: xml.NamespaceBinding = xml.TopScope): Unliftable[xml.Node] = Unliftable[xml.Node] {
    case q"${elem: xml.Elem}"     => elem
    case UnliftSpecialNode(snode) => snode
  }
}

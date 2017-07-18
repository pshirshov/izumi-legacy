package org.bitbucket.pshirshov.izumitk.json

import com.fasterxml.jackson.databind.node.ObjectNode
import org.bitbucket.pshirshov.izumitk.json.model.WithMeta

trait WithJsonTools {

  protected implicit class JsonExtensions(node: ObjectNode) {
    def field[T: Manifest](name: Symbol)(implicit mapper: JacksonMapper): T = {
      Option(node.get(name.name)) match {
        case Some(t) =>
          mapper.treeToValue[T](t)
        case None =>
          throw new IllegalArgumentException(s"Value `$name` is not defined in ${this.node}")
      }
    }
  }

  protected implicit class MetaExtensions(node: WithMeta) {
    def metaField[T: Manifest](name: Symbol)(implicit mapper: JacksonMapper): T = {
      JsonExtensions(node.meta).field[T](name)
    }

    def metaTo[T: Manifest](implicit mapper: JacksonMapper): T = mapper.treeToValue[T](node.meta)
  }

}

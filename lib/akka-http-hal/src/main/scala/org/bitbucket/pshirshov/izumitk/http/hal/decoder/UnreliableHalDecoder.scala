package org.bitbucket.pshirshov.izumitk.http.hal.decoder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ArrayNode, JsonNodeFactory, ObjectNode}
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.bitbucket.pshirshov.izumitk.util.types.TypeUtils


@Singleton
class UnreliableHalDecoder @Inject()
(
  @Named("permissiveMapper") mapper: JacksonMapper
) extends HalDecoder {

  import scala.collection.JavaConverters._
  import scala.reflect.runtime.universe._

  override def readHal[T: TypeTag : Manifest](halTree: ObjectNode): T = {
    val fixedTree = decodeHal[T](halTree)
    mapper.treeToValue[T](fixedTree)
  }


  override def readHal[T: TypeTag : Manifest](source: String): T = {
    readHal[T](mapper.readTree(source).asInstanceOf[ObjectNode])
  }

  private def decodeHal[T: TypeTag](source: ObjectNode): ObjectNode = {
    val enclosingTree = source.deepCopy()
    val tt = typeTag[T]

    Option(source.get("_embedded")) match {
      case Some(embedded: ObjectNode) =>

        embedded.fields().asScala.foreach {
          entry =>
            val attributeName = entry.getKey
            val attributeValue = entry.getValue

            if (attributeName.contains(":")) {
              throw new IllegalStateException(s"Namespaces unsupported: $attributeName, $enclosingTree")
            }


            tt.tpe.decls.find(_.name.decodedName.toString == attributeName) match {
              case None =>
                throw new IllegalStateException(s"No matching declaration for attribute: $attributeName, $tt")

              case Some(field) =>
                val fieldType = field.asMethod.returnType
                val fieldtt = TypeUtils.typeToTypeTag(fieldType, tt.mirror)

                fieldtt.tpe.asInstanceOf[TypeRefApi].args match {
                  case arg :: Nil if fieldType <:< typeOf[Option[_]] =>
                    val argTt = TypeUtils.typeToTypeTag(arg, tt.mirror)
                    toScalar(attributeValue).foreach {
                      e =>
                        enclosingTree.set(attributeName, decodeHal(e.asInstanceOf[ObjectNode])(argTt))
                    }

                  case Nil =>
                    toScalar(attributeValue).foreach {
                      e =>
                        enclosingTree.set(attributeName, decodeHal(e.asInstanceOf[ObjectNode])(fieldtt))
                    }

                  case arg :: Nil if fieldType <:< typeOf[Traversable[_]] =>
                    val asArray = toArray(attributeValue)
                    val argTt = TypeUtils.typeToTypeTag(arg, tt.mirror)
                    val newNode = mapper.getNodeFactory.arrayNode()
                    asArray.foreach {
                      e =>
                        newNode.add(decodeHal(e.asInstanceOf[ObjectNode])(argTt))
                    }
                    enclosingTree.set(attributeName, newNode)

                  case argKey :: argValue :: Nil =>
                    val argTt = TypeUtils.typeToTypeTag(argValue, tt.mirror)
                    enclosingTree.set(attributeName, decodeHal(enclosingTree)(argTt))

                  case _ =>
                    throw new IllegalStateException(s"Unexpected type args count: $attributeName, $tt")
                }
            }

        }

      case _ =>
    }

    tt.tpe.decls
      .filterNot(_.isSynthetic)
      .filter(_.isMethod).foreach {
      decl =>
        val fieldType = decl.asMethod.returnType
        val fieldtt = TypeUtils.typeToTypeTag(fieldType, tt.mirror)

        fieldtt.tpe.asInstanceOf[TypeRefApi].args match {
          case arg :: Nil if fieldType <:< typeOf[Traversable[_]] =>
            val fname = decl.name.decodedName.toString
            if (!enclosingTree.has(fname) || enclosingTree.get(fname).isNull) {
              enclosingTree.set(fname, JsonNodeFactory.instance.arrayNode())
            }

          case _ =>
        }
    }

    enclosingTree
  }


  private def toArray[T: TypeTag](attributeValue: JsonNode): List[JsonNode] = {
    if (!attributeValue.isArray) {
      throw new IllegalStateException(s"Array expected: $attributeValue")
    }

    val asArray = attributeValue.asInstanceOf[ArrayNode].elements().asScala.toList
    asArray
  }

  private def toScalar[T: TypeTag](attributeValue: JsonNode): Option[JsonNode] = {
    val asArray = toArray(attributeValue)
    if (asArray.size > 1) {
      throw new IllegalStateException(s"Not a scalar: $attributeValue")
    }
    asArray.headOption
  }
}



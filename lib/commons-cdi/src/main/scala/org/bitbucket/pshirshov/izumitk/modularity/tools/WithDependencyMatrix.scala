//package org.bitbucket.pshirshov.izumitk.modularity.tools
//
//import com.typesafe.scalalogging.StrictLogging
//import org.bitbucket.pshirshov.izumitk.util.CollectionUtils
//import org.bitbucket.pshirshov.izumitk.{Depends, NonRootPlugin}
//
///**
//  */
// TODO: ressurect or destroy?
//trait WithDependencyMatrix extends StrictLogging {
//  private def filterUnrequiredDependencies(plugins: Seq[Class[_]]) = {
//    val dependencyEdges = plugins.flatMap {
//      clz =>
//        Option(clz.getAnnotation(classOf[Depends])) match {
//          case Some(ann) =>
//            ann.value().map(dependency => clz -> dependency)
//          case None =>
//            Seq()
//        }
//    }
//
//    val dependencies = CollectionUtils.toMapOfSets[Class[_], Class[_]](dependencyEdges)
//    val reverseDependencies = CollectionUtils.toMapOfSets[Class[_], Class[_]](dependencyEdges.map(_.swap))
//
//    logger.debug(s"Dependency matrix: $dependencies")
//    logger.debug(s"Reverse dependency matrix: $reverseDependencies")
//
//    val withoutUnrequiredClasses = plugins.filter {
//      clz =>
//        Option(clz.getAnnotation(classOf[NonRootPlugin])) match {
//          case Some(ann) =>
//            val classes = (clz.getInterfaces :+ clz).toSet
//            // TODO: here we need to check all the dependency graph, not only first dependency level
//            val isOk = reverseDependencies.keySet.intersect(classes).nonEmpty
//            logger.debug(s"Non-root plugin `$clz` implements $classes and is necessary = $isOk")
//            isOk
//          case None =>
//            true
//        }
//    }
//    withoutUnrequiredClasses
//  }
//}


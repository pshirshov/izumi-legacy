package org.bitbucket.pshirshov.izumitk.util

/**
  */
object CollectionUtils {

    def toMapOfSets[A, B](list: Traversable[Tuple2[A, B]]): Map[A, Set[B]] = list.groupBy(_._1).map{case(x, xs) => (x, xs.map(_._2).toSet)}

//  def list2multimap[A, B](list: Traversable[Tuple2[A, B]]) =
//    list.groupBy(e => e._1).mapValues(e => e.map(x => x._2).toSet)

//  def list2multimap[A, B](list: Traversable[Tuple2[A, B]]) =
//    list.foldLeft(new mutable.HashMap[A, mutable.Set[B]] with mutable.MultiMap[A, B]){ (acc, pair) => acc.addBinding(pair._1, pair._2)}
}

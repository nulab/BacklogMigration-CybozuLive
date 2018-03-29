package com.nulabinc.backlog.c2b.services

import scala.collection.immutable.HashMap

object DiffPatch {

  type DiffMap = HashMap[String, String]

  def applyChanges(oldMap: DiffMap, newMap: DiffMap): DiffMap = {
    val step1 = oldMap.filter {
      case (left, _) =>
        newMap.contains(left)
    }
    val step2 = newMap.foldLeft(HashMap.empty[String, String]) {
      case (acc, (left, right)) =>
        val oldEntry = oldMap.get(left)
        acc + oldEntry.map(oldRight => (left, oldRight)).getOrElse((left, right))
    }
    step2
  }
}

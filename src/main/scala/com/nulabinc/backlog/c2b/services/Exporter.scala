package com.nulabinc.backlog.c2b.services

import com.nulabinc.backlog.c2b.datas.MappingContext
import com.nulabinc.backlog.c2b.interpreters.AppDSL
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL

object Exporter {

  def priorities()(implicit mappingContext: MappingContext): AppProgram[Unit] = {
    for {
      prioritiesStream <- AppDSL.fromDB(StoreDSL.getCybozuPriorities)
    } yield ()
  }

}

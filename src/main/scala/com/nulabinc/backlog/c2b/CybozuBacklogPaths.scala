package com.nulabinc.backlog.c2b

import better.files.File
import com.nulabinc.backlog.migration.common.conf.BacklogPaths


class CybozuBacklogPaths(backlogProjectKey: String) extends BacklogPaths(backlogProjectKey) {

  override def outputPath: File = File(Config.DATA_PATHS.toRealPath() + "/backlog")

}
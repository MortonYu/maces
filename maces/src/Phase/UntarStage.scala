package maces.phase

import maces._
import maces.annotation._

import os._

trait HasTarCache {
  var scratchPad: ScratchPad

  def tarCache: Path = scratchPad.get("system.tar_cache").get.asInstanceOf[DirectoryPathAnnotationValue].path
}

trait HasTarExternalFunction {
  def untar(file: Path, targetDirectory: Path) = {
    /** clear all existed files*/
    remove.all(targetDirectory)
    makeDir(targetDirectory)
    /** external dependency: tar */
    proc("tar", "xf", file, "-C", targetDirectory).call()
  }
}

case class UntarVendorFiles(scratchPadIn: ScratchPad) extends Phase with HasTarCache with HasTarExternalFunction {
  def transform: ScratchPad = {
    /** vendor.tars are std cell or PDK files provided by vendor. */
    val tarFiles: Seq[Path] = scratchPad.get("vendor.tars").get.asInstanceOf[TarsPathAnnotationValue].paths
    /** TODO: move this to InitializationStage*/
    makeDir(tarCache)
    tarFiles.foreach(f => untar(f, tarCache / f.last))
    scratchPad
  }
}

case class UntarInternalPackages(scratchPadIn: ScratchPad) extends Phase with HasTarCache with HasTarExternalFunction {
  /** the internal tar defined by user, that are RelPath to tarCache,
   * notice there is a sequence for internal tars,
   * Phase will extract them one by one, thus if there is any unordered tar,
   * Untar might will fail
   * */
  def transform: ScratchPad = {
    val tarFiles: Seq[Path] = scratchPad.get("vendor.internal_tars").get.asInstanceOf[RelTarsPathAnnotationValue].paths.map(t => Path(t, tarCache))
    val tarFilesRenamed: Seq[Path] = tarFiles.map { t =>
      val renamedPath = Path(t.toString + ".tmp")
      move(t, renamedPath)
      renamedPath
    }
    (tarFilesRenamed zip tarFiles) foreach (pair => untar(pair._1, pair._2))
    tarFilesRenamed foreach remove.all
    scratchPad
  }
}

case class UntarStage(scratchPadIn: ScratchPad) extends Stage {
  def phases: Seq[ScratchPad => Phase] = Seq(
    UntarVendorFiles,
    UntarInternalPackages
  )
}
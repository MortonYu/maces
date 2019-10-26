package maces.phase

import maces._
import maces.annotation._

import ammonite.ops._
import ammonite.ops.ImplicitWd._

trait HasTarCache {
  var scratchPad: ScratchPad

  def tarCache: Path = scratchPad.get("system.tar_cache").get.asInstanceOf[DirectoryPathAnnotationValue].path
}

trait HasTarExternalFunction {
  def untar(file: Path, targetDirectory: Path) = {
    mkdir ! targetDirectory

    /** external dependency: tar */
    %%('tar, "xf", file, "-C", targetDirectory)
  }
}

case class UntarVendorFiles(scratchPadIn: ScratchPad) extends Phase with HasTarCache with HasTarExternalFunction {
  def transform: ScratchPad = {
    /** vendor.tars are std cell or PDK files provided by vendor. */
    val tarFiles: Seq[Path] = scratchPad.get("vendor.tars").get.asInstanceOf[TarsPathAnnotationValue].paths
    mkdir ! tarCache
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
      mv(t, renamedPath)
      renamedPath
    }
    (tarFilesRenamed zip tarFiles) foreach (pair => untar(pair._1, pair._2))
    tarFilesRenamed foreach rm
    scratchPad
  }
}

case class UntarStage(scratchPadIn: ScratchPad) extends Stage {
  def phases: Seq[ScratchPad => Phase] = Seq(
    UntarVendorFiles,
    UntarInternalPackages
  )
}
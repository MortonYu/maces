package maces.phase

import maces._
import maces.annotation._

import ammonite.ops._
import ammonite.ops.ImplicitWd._

case class UntarVendorFiles(scratchPadIn: ScratchPad) extends Phase {
  def untar(file: Path, targetDirectory: Path) = %%('tar, "xf", file, "-C", targetDirectory)

  def transform(scratchPad: ScratchPad): ScratchPad = {
    /** collect necessary annotations */
    val tarFiles: Seq[Path] = scratchPad.get("vendor.tars").get.asInstanceOf[PathsAnnotation].paths
    val workspace: Path = scratchPad.get("user.workspace").get.asInstanceOf[PathAnnotation].path
    val tarCache: Path = Path(scratchPad.get("system.tar_cache").get.asInstanceOf[RelPathAnnotation].path, workspace)
    mkdir ! tarCache
    tarFiles.foreach(f => untar(f, tarCache/f.baseName))
    scratchPad
  }
}


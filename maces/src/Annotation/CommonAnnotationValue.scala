package maces.annotation

import maces._
import os._


case class TarsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class RelTarsPathAnnotationValue(paths: Seq[RelPath]) extends RelPathsAnnotationValue

case class BinPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoryPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoriesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class HdlsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class LefsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class LibertyCellLibrariesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class InstanceNameAnnotationValue(value: String) extends AnnotationValue

case class CoreLimitAnnotationValue(value: Int) extends AnnotationValue

case class AutoClockGatingAnnotationValue(value: Boolean) extends AnnotationValue

case class ClockGateCellPrefixAnnotationValue(value: String) extends AnnotationValue

case class CornerValue(name: String, cornerType: String, voltage: Double, temperature: Double, lib: Path, qrcTech: Path) {
  assert(cornerType == "setup" | cornerType == "hold")
}

case class CornerValuesAnnotationValue(value: Seq[CornerValue]) extends AnnotationValue

trait HasWorkspace {
  var scratchPad: ScratchPad

  def workspace: Path = scratchPad.get("system.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path
}
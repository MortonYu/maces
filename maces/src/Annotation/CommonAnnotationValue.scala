package maces.annotation

import maces._
import os._


case class TarsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class RelTarsPathAnnotationValue(paths: Seq[RelPath]) extends RelPathsAnnotationValue

case class BinPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoryPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoriesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class VerilogsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class LibertyCellLibrariesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class InstanceNameAnnotationValue(value: String) extends AnnotationValue


trait HasWorkspace {
  var scratchPad: ScratchPad

  def workspace: Path = scratchPad.get("system.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path
}
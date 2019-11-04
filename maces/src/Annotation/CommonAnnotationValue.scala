package maces.annotation

import maces._
import os._
import scala.math.Ordered.orderingToOrdered

case class TarsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class RelTarsPathAnnotationValue(paths: Seq[RelPath]) extends RelPathsAnnotationValue

case class BinPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoryPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoriesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class HdlPathAnnotationValue(path: Path) extends PathAnnotationValue

case class SdcPathAnnotationValue(path: Path) extends PathAnnotationValue

case class SdfPathAnnotationValue(path: Path) extends PathAnnotationValue

case class LefPathAnnotationValue(path: Path) extends PathAnnotationValue

case class HdlsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class SdcsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class SdfsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class LefsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class LibertyCellLibrariesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class InstanceNameAnnotationValue(value: String) extends AnnotationValue

case class CoreLimitAnnotationValue(value: Int) extends AnnotationValue

case class AutoClockGatingAnnotationValue(value: Boolean) extends AnnotationValue

case class CellPrefixAnnotationValue(value: String) extends AnnotationValue

case class CornerValue(name: String, cornerType: String, voltage: Double, temperature: Double, lib: Path, qrcTech: Path) extends Ordered[CornerValue] {
  assert(cornerType == "setup" | cornerType == "hold")
  override def compare(that: CornerValue): Int = (this.voltage, this.temperature) compare (that.voltage, that.temperature)
}

case class CornerValuesAnnotationValue(value: Seq[CornerValue]) extends AnnotationValue

trait HasWorkspace {
  var scratchPad: ScratchPad

  def workspace: Path
}
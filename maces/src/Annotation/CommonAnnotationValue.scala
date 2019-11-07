package maces.annotation

import maces._
import os._
import scala.math.Ordered.orderingToOrdered

case class TarsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class RelTarsPathAnnotationValue(paths: Seq[RelPath]) extends RelPathsAnnotationValue

case class BinPathAnnotationValue(path: Path) extends PathAnnotationValue

case class EnvAnnotationValue(value: Map[String, String]) extends AnnotationValue

case class DirectoryPathAnnotationValue(path: Path) extends PathAnnotationValue

case class DirectoriesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class HdlPathAnnotationValue(path: Path) extends PathAnnotationValue

case class SdcPathAnnotationValue(path: Path) extends PathAnnotationValue

case class SdfPathAnnotationValue(path: Path) extends PathAnnotationValue

case class TclPathAnnotationValue(path: Path) extends PathAnnotationValue

case class LefPathAnnotationValue(path: Path) extends PathAnnotationValue

case class HdlsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class SdcsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class SdfsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class LefsPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class LibertyCellLibrariesPathAnnotationValue(paths: Seq[Path]) extends PathsAnnotationValue

case class InstanceNameAnnotationValue(value: String) extends AnnotationValue

case class CoreLimitAnnotationValue(value: Int) extends AnnotationValue

case class AreaAnnotationValue(value: Double) extends AnnotationValue

case class FanOutAnnotationValue(value: Double) extends AnnotationValue

case class PowerAnnotationValue(value: Double) extends AnnotationValue

case class AutoClockGatingAnnotationValue(value: Boolean) extends AnnotationValue

case class CellNameAnnotationValue(value: String) extends AnnotationValue

case class StreamDataAnnotationValue(value: String) extends AnnotationValue

case class Library(name: String,
                   voltage: Double = 1.8,
                   temperature: Double = 25,
                   nominalType: String = "tt",
                   libertyFile: Option[Path] = None,
                   qrcTechFile: Option[Path] = None,
                   vsimFile: Option[Path] = None,
                   itfFile: Option[Path] = None,
                   lefFile: Option[Path] = None,
                   spiceFile: Option[Path] = None,
                   gdsFile: Option[Path] = None) {
  nominalType.foreach(c => require(Set('s', 't', 'f').contains(c)))
  require(nominalType.length == 2)
}

case class LibrariesAnnotationValue(value: Seq[Library]) extends AnnotationValue

case class Corner(name: String,
                  timingType: String,
                  voltage: Double,
                  temperature: Double) extends Ordered[Corner] {
  override def compare(that: Corner): Int = (this.voltage, this.temperature).compare(that.voltage, that.temperature)
}

case class CornersAnnotationValue(value: Seq[Corner]) extends AnnotationValue

trait HasWorkspace {
  var scratchPad: ScratchPad

  def workspace: Path
}
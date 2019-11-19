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

case class ProcessNodeAnnotationValue(value: Int) extends AnnotationValue

case class AreaAnnotationValue(value: Double) extends AnnotationValue

case class VoltageAnnotationValue(value: Double) extends AnnotationValue

case class FanOutAnnotationValue(value: Double) extends AnnotationValue

case class PowerAnnotationValue(value: Double) extends AnnotationValue

case class AutoClockGatingAnnotationValue(value: Boolean) extends AnnotationValue

case class CellNameAnnotationValue(value: String) extends AnnotationValue

case class CellsNameAnnotationValue(value: Seq[String]) extends AnnotationValue

case class WireNameAnnotationValue(value: String) extends AnnotationValue

case class TimeUnitAnnotationValue(value: String) extends AnnotationValue

case class PowerSpecModeAnnotationValue(value: String) extends AnnotationValue {
  require(Set("auto", "manual", "empty").contains(value))
}

case class PowerSpecTypeAnnotationValue(value: String) extends AnnotationValue {
  require(Set("upf", "cpf").contains(value))
}

case class StreamDataAnnotationValue(value: String) extends AnnotationValue

case class DesignEffortAnnotationValue(value: String) extends AnnotationValue {
  require(Set("express", "standard", "extreme").contains(value))
}

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

case class HierarchicalModeAnnotationValue(value: String) extends AnnotationValue {
  require(Set("flat", "leaf", "hierarchical", "top").contains(value))
}

case class LibrariesAnnotationValue(value: Seq[Library]) extends AnnotationValue

case class Corner(name: String,
                  timingType: String,
                  voltage: Double,
                  temperature: Double) extends Ordered[Corner] {
  override def compare(that: Corner): Int = (this.voltage, this.temperature).compare(that.voltage, that.temperature)
}

case class CornersAnnotationValue(value: Seq[Corner]) extends AnnotationValue

case class Ilm(dir: Path, dataDir: Path, module: String, lef: Path, gds: Path, netlist: Path)

case class IlmsAnnotationValue(value: Seq[Ilm]) extends AnnotationValue

case class Supply(name: String, supplyType: String, pin: String, tie: String, weight: Int, voltage: Double) {
  require(Set("ground", "power").contains(supplyType))
}

case class SuppliesAnnotationValue(value: Seq[Supply]) extends AnnotationValue

case class Margins(left: Double,
                   bottom: Double,
                   right: Double,
                   top: Double)

/**
 * @param path             Path to the given instance
 * @param placementType    The type of placement constraint:
 *                         -  "dummy":  does nothing with this constraint
 *                         -  "placement":  creates a placement constraint for an instance
 *                         -  "toplevel":  top-level chip dimensions; may only occur once, for the top-level module
 *                         -  "hardmacro":  places this hard macro at a particular spot
 *                         -  "hierarchical":  marks this instance as part of hierarchical place and route
 *                         -  "obstruction":  creates a blockage at a particular spot; see obs_types
 * @param x                x coordinate in um
 * @param y                y coordinate in um
 * @param width            can be auto-filled for hierarchical and hardmacro if left blank
 * @param height           can be auto-filled for hierarchical and hardmacro if left blank
 * @param master           The master module name of the hierarchical or hardmacro constraint.
 *                         For example, if this hardmacro is an instance of "my_amplifier", this
 *                         field should be set to "my_amplifier".
 *                         Required for hierarchical and optional for hardmacro, disallowed otherwise
 * @param createPhysical   Create an instance of a physical-only cell that does not exist in addition to placing it
 *                         Optional for hardmacro, disallowed otherwise
 * @param orientation      orientation (Optional[str]) - The orientation
 *                         -  "r0":  standard orientation
 *                         -  "r90":  rotated 90 degrees clockwise
 *                         -  "r180":  rotated 180 degrees
 *                         -  "r270":  rotated 270 degrees clockwise; equivalent to -90 degrees counterclockwise
 *                         -  "mx":  mirrored about the x-axis
 *                         -  "mx90":  mirrored about the x-axis, then rotated 90 degrees clockwise
 *                         -  "my":  mirrored about the y-axis
 *                         -  "my90":  mirrored about the y-axis, then rotated 90 degrees clockwise
 * @param margins          margins for the top-level module.
 *                         -  "left"   (margin from the left side of the core PNR area to the edge of the chip)
 *                         -  "right"  (margin from the right side of the core PNR area to the edge of the chip)
 *                         -  "top"    (margin from the top side of the core PNR area to the edge of the chip)
 *                         -  "bottom" (margin from the bottom side of the core PNR area to the edge of the chip)
 *                         Required for toplevel, disallowed otherwise
 * @param topLayer         Specifies the highest layer used by this hierarchical or hardmacro, used to keep other wires away
 *                         Optional for hierarchical and hardmacro
 * @param layers           A list of strings that enumerates layer(s) blocked by the obstruction otherwise all layers are blocked
 *                         Required for the obstruction type, disallowed otherwise
 * @param obstructionTypes A list of the types of obstructions for the given geometry
 *                         Required for the obstruction type, disallowed otherwise
 *                         -  "place" - This obstruction type stops the placement of standard cells inside it
 *                         -  "route" - This obstruction type stops all routing inside it
 *                         -  "power" - This obstruction type stops only power routing/straps inside it
 **/
case class PlacementConstraint(path: String,
                               placementType: String,
                               x: Double,
                               y: Double,
                               width: Double,
                               height: Double,
                               master: Option[String] = None,
                               createPhysical: Boolean = false,
                               orientation: String = "r0",
                               margins: Option[Margins],
                               topLayer: Option[String],
                               layers: Option[Seq[String]],
                               obstructionTypes: Option[Seq[String]])

case class PlacementConstraintsAnnotationValue(values: Seq[PlacementConstraint]) extends AnnotationValue

trait HasWorkspace {
  var scratchPad: ScratchPad

  def workspace: Path
}
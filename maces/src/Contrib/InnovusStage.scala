package maces.contrib.cadence

import maces._
import maces.annotation._
import maces.phase._
import os._

case class InnovusStage(scratchPadIn: ScratchPad) extends CliStage {

  implicit class CadenceCornerValue(c: Corner) {
    def matchLibraries(libraries: Seq[Library]): Seq[Library] = libraries.filter(l => l.voltage == c.voltage & l.temperature == c.temperature)

    def qrcFile(libraries: Seq[Library]): Path = {
      /** only one RC module */
      val f = libraries.map(_.qrcTechFile).toSet
      assert(f.size == 1)
      f.head.get
    }

    def mmmcString(libraries: Seq[Library]) =
      s"""create_library_set -name ${c.name}.${c.timingType}_set -timing [list ${c.matchLibraries(libraries).map(_.libertyFile.get.toString).reduce(_ + " " + _)}]
         |create_timing_condition -name ${c.name}.${c.timingType}_cond -library_sets [list ${c.name}.${c.timingType}_set]
         |create_rc_corner -name ${c.name}.${c.timingType}_rc -temperature ${c.temperature} -qrc_tech ${qrcFile(libraries).toString}
         |create_delay_corner -name ${c.name}.${c.timingType}_delay -timing_condition ${c.name}.${c.timingType}_cond -rc_corner ${c.name}.${c.timingType}_rc
         |create_analysis_view -name ${c.name}.${c.timingType}_view -delay_corner ${c.name}.${c.timingType}_delay -constraint_mode my_constraint_mode""".stripMargin
  }

  implicit class CadenceFloorPlanValue(f: PlacementConstraint) {
    def layerString: String = f.layers match {
      case Some(ss) => ss.reduce(_ + " " + _)
      case None => "all"
    }

    def floorplanString: String = {
      if (f.createPhysical)
        s"""create_inst -cell ${f.master.get} -inst ${f.path} -location {${f.x} ${f.y}} -orient ${f.orientation} -physical -status fixed
           |place_inst ${f.path} ${f.x} ${f.y} ${f.orientation} -fixed
           |""".stripMargin
      else f.placementType match {
        case "toplevel" => s"create_floorplan -core_margins_by die -flip f -die_size_by_io_height max $site -die_size { ${f.width} ${f.height} ${f.margins.get.left} ${f.margins.get.bottom} ${f.margins.get.right} ${f.margins.get.top} }"

        /** what is createPhysical */
        case "placement" => s"create_guide -name ${f.path} -area ${f.x} ${f.y} ${f.x + f.width} ${f.y + f.height}"

        /** TODO: add stackup */
        case "hardmacro" | "hierarchical" => s"place_inst ${f.path} ${f.x} ${f.y} ${f.orientation}"
        case "obstruction" => {
          val obstructionTypes: Seq[String] = f.obstructionTypes.get
          s"""${if (obstructionTypes.contains("place")) s"create_place_blockage -area {${f.x} ${f.y} ${f.x + f.width} ${f.y + f.height}}"}
             |${if (obstructionTypes.contains("route")) s"create_route_blockage -layers {$layerString} -spacing 0 -rects {${f.x} ${f.y} ${f.x + f.width} ${f.y + f.height}}"}
             |${if (obstructionTypes.contains("power")) s"create_route_blockage -pg_nets -layers {$layerString}} -rects {${f.x} ${f.y} ${f.x + f.width} ${f.y + f.height}}"}
             |""".stripMargin
        }
      }

    }
  }

  implicit class CadenceIlmRead(i: Ilm) {
    def read = s"read_ilm -cell ${i.module} -directory ${i.dir}\n"
  }

  override def workspace: Path = scratchPad.get("runtime.innovus.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path

  override def env: Map[String, String] = scratchPad.get("runtime.innovus.env").get.asInstanceOf[EnvAnnotationValue].value

  def coreLimit: Int = scratchPad.get("runtime.innovus.core_limit").get.asInstanceOf[CoreLimitAnnotationValue].value

  def processNode: Int = scratchPad.get("runtime.innovus.process_node").get.asInstanceOf[ProcessNodeAnnotationValue].value

  def bin: Path = scratchPad.get("runtime.innovus.bin").get.asInstanceOf[BinPathAnnotationValue].path

  def netlists: Seq[String] = scratchPad.get("runtime.innovus.netlists").get.asInstanceOf[HdlsPathAnnotationValue].paths.map(_.toString)

  def topName: String = scratchPad.get("runtime.innovus.top_name").get.asInstanceOf[InstanceNameAnnotationValue].value

  def powerName: String = scratchPad.get("runtime.innovus.power_name").get.asInstanceOf[WireNameAnnotationValue].value

  def groundName: String = scratchPad.get("runtime.innovus.ground_name").get.asInstanceOf[WireNameAnnotationValue].value

  def timeUnit: String = scratchPad.get("runtime.innovus.time_unit").get.asInstanceOf[TimeUnitAnnotationValue].value

  def site: String = ???

  def lefs: Seq[String] = scratchPad.get("runtime.innovus.tech_lef_files").get.asInstanceOf[LefsPathAnnotationValue].paths.map(_.toString)

  def hierarchicalMode: String = scratchPad.get("runtime.innovus.hierarchical_mode").get.asInstanceOf[HierarchicalModeAnnotationValue].value

  def ilms: Seq[Ilm] = scratchPad.get("runtime.innovus.ilms").get.asInstanceOf[IlmsAnnotationValue].value

  def floorplans: Seq[PlacementConstraint] = scratchPad.get("runtime.innovus.floorplan").get.asInstanceOf[PlacementConstraintsAnnotationValue].values

  def nonLeaf: Boolean = hierarchicalMode match {
    /** is_nonleaf_hierarchical */
    case "hierarchical" => true
    case "top" => true
    case _ => false
  }

  def powerSpecMode: String = scratchPad.get("runtime.innovus.power_spec_mode").get.asInstanceOf[PowerSpecModeAnnotationValue].value

  def supplies: Seq[Supply] = scratchPad.get("runtime.innovus.supplies").get.asInstanceOf[SuppliesAnnotationValue].value

  def powerNets: Seq[Supply] = supplies.filter(_.supplyType == "power")

  def groundNets: Seq[Supply] = supplies.filter(_.supplyType == "ground")

  def designEffort: String = scratchPad.get("runtime.innovus.designEffort").get.asInstanceOf[DesignEffortAnnotationValue].value

  def dontUse: Seq[String] = scratchPad.get("runtime.innovus.dont_use").get.asInstanceOf[CellsNameAnnotationValue].value

  def autoClockGating: Boolean = scratchPad.get("runtime.innovus.auto_clock_gate").get.asInstanceOf[AutoClockGatingAnnotationValue].value

  def clockGateCellPrefix: String = scratchPad.get("runtime.innovus.clock_gate_cell_prefix").get.asInstanceOf[CellNameAnnotationValue].value

  def mmmcTcl: String = scratchPad.get("runtime.innovus.mmmc_tcl").get.asInstanceOf[TclPathAnnotationValue].path.toString

  def libraries: Seq[Library] = scratchPad.get("runtime.innovus.libraries").get.asInstanceOf[LibrariesAnnotationValue].value

  def debug = true

  class waitInit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = ""

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        str.contains("@innovus:root: 1>")
      }
      assert(r._1)
      (scratchPad, Some(new defaultSettings(scratchPad)))
    }
  }

  class defaultSettings(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""set_db design_process_node $processNode
         |set_multi_cpu_usage -local_cpu $coreLimit
         |set_db timing_analysis_cppr both
         |set_db timing_analysis_type ocv
         |set_library_unit -time 1$timeUnit
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      (scratchPad, Some(new readPhysical(scratchPad)))
    }
  }

  class readPhysical(var scratchPad: ScratchPad) extends ProcessNode {
    val lefFiles = lefs ++ libraries.filter(_.lefFile.nonEmpty).map(_.lefFile.get.toString).distinct ++ (if (nonLeaf) ilms.map(_.lef.toString) else Seq[String]())

    def input: String = s"read_physical -lef { ${lefFiles.reduce(_ + " " + _)} }\n"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      (scratchPad, Some(new readMMMC(scratchPad)))
    }
  }

  class readMMMC(var scratchPad: ScratchPad) extends ProcessNode {
    /** currently we should already mmmc.tcl generated from genus. */
    def input: String = s"read_mmmc $mmmcTcl"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      (scratchPad, Some(new readNetlist(scratchPad)))
    }
  }

  class readNetlist(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""read_netlist { ${netlists.reduce(_ + " " + _)} } -top $topName
         |${ilms.map(_.read)}
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      val nextStage = if (nonLeaf) new readIlm(scratchPad) else new powerSpec(scratchPad)
      (scratchPad, Some(nextStage))
    }
  }

  class readIlm(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = ilms.map(_.read).reduce(_ + _)

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      val nextStage = if (powerSpecMode == "empty") new powerSpec(scratchPad) else new initDesign(scratchPad)
      (scratchPad, Some(nextStage))
    }
  }

  class powerSpec(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""set_db init_power_nets { $powerName }
         |set_db init_ground_nets { $groundName }
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      (scratchPad, Some(new initDesign(scratchPad)))
    }
  }

  class initDesign(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""init_design
         |set_db design_flow_effort $designEffort""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      (scratchPad, Some(new floorplan(scratchPad)))
    }
  }

  class floorplan(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = floorplans.map(_.floorplanString).reduce(_ + "\n" + _)

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      (scratchPad, Some(new route(scratchPad)))
    }
  }

  class route(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""route_design
         |opt_design -post_route -setup -hold
         |set_db write_stream_virtual_connection false
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      println(waitString(5))
      (scratchPad, Some(new initDesign(scratchPad)))
    }
  }

  class exit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "quit\n"
  }

  override val node: ProcessNode = new waitInit(scratchPadIn)


  def command: Seq[String] = {
    Seq(bin.toString)
  }
}


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
        case "toplevel" => s"create_floorplan -core_margins_by die -flip f -die_size_by_io_height max -site $site -die_size { ${f.width} ${f.height} ${f.margins.get.left} ${f.margins.get.bottom} ${f.margins.get.right} ${f.margins.get.top} }"

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

  def stdinTclPath: Path = Path(scratchPad.get("runtime.innovus.stdin_shell").get.asInstanceOf[GeneratedFileAnnotationValue].path, generatedPath)

  def enterPath: Path = Path(scratchPad.get("runtime.innovus.enter_shell").get.asInstanceOf[GeneratedFileAnnotationValue].path, generatedPath)

  def coreLimit: Int = scratchPad.get("runtime.innovus.core_limit").get.asInstanceOf[CoreLimitAnnotationValue].value

  def processNode: Int = scratchPad.get("runtime.innovus.process_node").get.asInstanceOf[ProcessNodeAnnotationValue].value

  def bin: Path = scratchPad.get("runtime.innovus.bin").get.asInstanceOf[BinPathAnnotationValue].path

  def netlists: Seq[String] = scratchPad.get("runtime.innovus.netlists").get.asInstanceOf[HdlsPathAnnotationValue].paths.map(_.toString)

  def topName: String = scratchPad.get("runtime.innovus.top_name").get.asInstanceOf[InstanceNameAnnotationValue].value

  def powerName: String = scratchPad.get("runtime.innovus.power_name").get.asInstanceOf[WireNameAnnotationValue].value

  def groundName: String = scratchPad.get("runtime.innovus.ground_name").get.asInstanceOf[WireNameAnnotationValue].value

  def timeUnit: String = scratchPad.get("runtime.innovus.time_unit").get.asInstanceOf[TimeUnitAnnotationValue].value

  def site: String = scratchPad.get("runtime.innovus.site").get.asInstanceOf[SiteNameAnnotationValue].value

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
      val r = waitUntil(100) { str =>
        str.contains("@innovus 1>")
      }
      assert(r._1)

      (scratchPad, Some(new defaultSettings(scratchPad)))
    }
  }

  class defaultSettings(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""set_multi_cpu_usage -local_cpu $coreLimit
         |set_library_unit -time 1$timeUnit
         |set_db design_process_node $processNode
         |set_db timing_analysis_cppr both
         |set_db timing_analysis_type ocv
         |""".stripMargin


    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(100) { str =>
        Set(
          str.contains("1 28"),
          str.contains("1 both"),
          str.contains("1 ocv")
        ).reduce(_ & _)
      }
      assert(r._1)
      (scratchPad, Some(new readPhysical(scratchPad)))
    }
  }

  class readPhysical(var scratchPad: ScratchPad) extends ProcessNode {
    val lefFiles = lefs ++ libraries.filter(_.lefFile.nonEmpty).map(_.lefFile.get.toString).distinct ++ (if (nonLeaf) ilms.map(_.lef.toString) else Seq[String]())

    def input: String = s"read_physical -lef { ${lefFiles.reduce(_ + " " + _)} }\n"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        Set(
          str.contains("viaInitial starts at"),
          str.contains("viaInitial ends")
        ).reduce(_ & _)
      }
      assert(r._1)
      (scratchPad, Some(new readMMMC(scratchPad)))
    }
  }

  class readMMMC(var scratchPad: ScratchPad) extends ProcessNode {
    /** currently we should already mmmc.tcl generated from genus. */
    def input: String =
      s"""read_mmmc $mmmcTcl
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        str.contains("timing_initialized")
      }
      assert(r._1)
      (scratchPad, Some(new readNetlist(scratchPad)))
    }
  }

  class readNetlist(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""read_netlist { ${netlists.reduce(_ + " " + _)} } -top $topName
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        str.contains("Netlist is unique.")
      }
      assert(r._1)
      val nextStage = new powerSpec(scratchPad)
      (scratchPad, Some(nextStage))
    }
  }

  class powerSpec(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""set_db init_power_nets { $powerName }
         |set_db init_ground_nets { $groundName }
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        Set(
          str.contains("1  VDD"),
          str.contains("1  VDD")
        ).reduce(_ & _)
      }
      assert(r._1)
      (scratchPad, Some(new initDesign(scratchPad)))
    }
  }

  class initDesign(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""init_design
         |set_db design_flow_effort $designEffort
         |""".stripMargin

    def expressRoute: String = if (designEffort == "express") s"set setDesignmode design_express_route true\n" else ""


    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        str.contains(s"1 $designEffort")
      }
      assert(r._1)

      (scratchPad, Some(new floorplan(scratchPad)))
    }
  }

  class floorplan(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = floorplans.map(_.floorplanString).reduce(_ + "\n" + _) + "\nplace_opt_design\n"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(120) { str =>
        str.contains("Finished GigaPlace")
      }
      assert(r._1)

      (scratchPad, Some(new route(scratchPad)))
    }
  }

  class route(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""route_design
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(120) { str =>
        str.contains("End route_design")
      }
      assert(r._1)
      (scratchPad, Some(new finalOpt(scratchPad)))
    }
  }

  class finalOpt(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""opt_design -post_route -setup -hold
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(120) { str =>
        str.contains("Finished opt_design")
      }
      assert(r._1)
      (scratchPad, Some(new save(scratchPad)))
    }
  }

  class save(var scratchPad: ScratchPad) extends ProcessNode {
    def innovusDb: Path = generatedPath / (topName + "_database")

    def qorJson: Path = workspace / "qor.json"

    def input: String =
      s"""set_db write_stream_virtual_connection false
         |report_qor -format json -file ${qorJson.toString}
         |write_db ${innovusDb.toString}
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(30) { str =>
        str.contains("End write_db save design")
      }
      assert(r._1)

      /** read qor json and convert necessary information here */
      val j = ujson.read(read(qorJson))("snapshots").arr.map(m => m("name").str -> m).toMap

      /** we currently not implement the metadata system,
       * in case of phase with broken links,
       * we copy all targets and remove broken link caused by EDA bug.
       * */
      os.walk(pwd, followLinks = false).filter(os.isLink).foreach { l =>
        val t = os.readLink.absolute(l)
        if (os.exists(t)) {
          os.copy.over(t, l)
        } else {
          os.remove.all(l)
        }
      }

      def getAreaFromJson(name: String, key: String): Double = j(name)("metrics").arr.filter(k => k("name").str == key).head("value").str.replace(" um^2", "").toDouble

      def getFinalAreaFromJson(key: String): Double = getAreaFromJson("route_design", key)

      scratchPad = scratchPad add
        Annotation("runtime.innovus.export_db", InnovusDbPathAnnotationValue(innovusDb)) add
        Annotation("runtime.innovus.cell_area", AreaAnnotationValue(getFinalAreaFromJson("design.area"))) add
        Annotation("runtime.innovus.area_io_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.area_io"))) add
        Annotation("runtime.innovus.blackbox_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.blackbox"))) add
        Annotation("runtime.innovus.buffer_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.buffer"))) add
        Annotation("runtime.innovus.combinatorial_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.combinatorial"))) add
        Annotation("runtime.innovus.icg_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.icg"))) add
        Annotation("runtime.innovus.inverter_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.inverter"))) add
        Annotation("runtime.innovus.io_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.io"))) add
        Annotation("runtime.innovus.iso_ls_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.iso_ls"))) add
        Annotation("runtime.innovus.latch_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.latch"))) add
        Annotation("runtime.innovus.logical_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.logical"))) add
        Annotation("runtime.innovus.macro_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.macro"))) add
        Annotation("runtime.innovus.physical_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.physical"))) add
        Annotation("runtime.innovus.power_switch_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.power_switch"))) add
        Annotation("runtime.innovus.register_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.register"))) add
        Annotation("runtime.innovus.sequential_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.sequential"))) add
        Annotation("runtime.innovus.std_cell_area", AreaAnnotationValue(getFinalAreaFromJson("design.area.std_cell")))

      (scratchPad, Some(new exit(scratchPad)))
    }
  }

  class exit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "exit\n"

    write(stdinTclPath, stdinLogger.toString)
    write(enterPath, "#!/bin/bash\n" + env.map(m => s"export ${m._1}=${m._2}\n").reduce(_ + _) + command.reduce(_ + " " + _), perms = "r-x------")
  }

  override val node: ProcessNode = new waitInit(scratchPadIn)

  def command: Seq[String] = {
    Seq(bin.toString, "-nowin", "-common_ui")
  }
}
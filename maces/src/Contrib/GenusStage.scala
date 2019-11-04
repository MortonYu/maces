package maces.contrib.cadence


import maces._
import maces.annotation._
import maces.phase._
import os._

case class GenusStage(scratchPadIn: ScratchPad) extends CliStage {

  implicit class CadenceCornerValue(c: CornerValue) {
    def mmmcString =
      s"""
         |create_library_set -name ${c.name}.${c.cornerType}_set -timing [list ${c.lib.toString}]
         |create_timing_condition -name ${c.name}.${c.cornerType}_cond -library_sets [list ${c.name}.${c.cornerType}_set]
         |create_rc_corner -name ${c.name}.${c.cornerType}_rc -temperature ${c.temperature} -qrc_tech ${c.qrcTech.toString}
         |create_delay_corner -name ${c.name}.${c.cornerType}_delay -timing_condition ${c.name}.${c.cornerType}_cond -rc_corner ${c.name}.${c.cornerType}_rc
         |create_analysis_view -name ${c.name}.${c.cornerType}_view -delay_corner ${c.name}.${c.cornerType}_delay -constraint_mode my_constraint_mode
         |""".stripMargin
  }

  override def workspace: Path = scratchPad.get("runtime.genus.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path

  override def env: Map[String, String] = scratchPad.get("runtime.genus.env").get.asInstanceOf[EnvAnnotationValue].value

  def bin: Path = scratchPad.get("user.bin.genus").get.asInstanceOf[BinPathAnnotationValue].path

  def hdls: Seq[String] = scratchPad.get("runtime.genus.hdl_files").get.asInstanceOf[HdlsPathAnnotationValue].paths.map(_.toString)

  def lefs: Seq[String] = scratchPad.get("runtime.genus.lef_files").get.asInstanceOf[LefsPathAnnotationValue].paths.map(_.toString)

  def libertyLibraries: Seq[String] = scratchPad.get("runtime.genus.liberty_cell_files").get.asInstanceOf[LibertyCellLibrariesPathAnnotationValue].paths.map(_.toString)

  def topName: String = scratchPad.get("runtime.genus.top_name").get.asInstanceOf[InstanceNameAnnotationValue].value

  def coreLimit: Int = scratchPad.get("runtime.genus.core_limit").get.asInstanceOf[CoreLimitAnnotationValue].value

  def autoClockGating: Boolean = scratchPad.get("runtime.genus.auto_clock_gate").get.asInstanceOf[AutoClockGatingAnnotationValue].value

  def clockGateCellPrefix: String = scratchPad.get("runtime.genus.clock_gate_cell_prefix").get.asInstanceOf[SdcPathAnnotationValue].path.toString

  def clockConstrain: String = scratchPad.get("runtime.genus.clock_constrain_file").get.asInstanceOf[SdcPathAnnotationValue].path.toString

  def pinConstrain: String = scratchPad.get("runtime.genus.pin_constrain_file").get.asInstanceOf[SdcPathAnnotationValue].path.toString

  def tie0Cell: String = scratchPad.get("runtime.genus.tie0_cell").get.asInstanceOf[CellPrefixAnnotationValue].value

  def tie1Cell: String = scratchPad.get("runtime.genus.tie1_cell").get.asInstanceOf[CellPrefixAnnotationValue].value

  def corners: Seq[CornerValue] = scratchPad.get("runtime.genus.mmmc_corners").get.asInstanceOf[CornerValuesAnnotationValue].value

  def worstCorner: CornerValue = corners.min

  def mmmcFile: Path = {
    val f = workspace / "mmmc.tcl"
    corners.foreach(c => write(f, c.mmmcString))
    f
  }

  class waitInit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = ""

    override def should: (ScratchPad, Option[ProcessNode]) = {
      assert(waitUntil(20) { str =>
        str.contains("@genus:root: 1>")
      }._1)
      (scratchPad, Some(new defaultSettings(scratchPad)))
    }
  }

  class defaultSettings(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""
         |set_db hdl_error_on_blackbox true
         |set_db max_cpus_per_server $coreLimit
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      assert(waitUntil(1) { str =>
        Set(
          str.contains("Setting attribute of root '/': 'hdl_error_on_blackbox' = true"),
          str.contains("Setting attribute of root '/': 'max_cpus_per_server' = 8")
        ).reduce(_ & _)
      }._1)
      val nextNode = if (autoClockGating) new clockGatingSettings(scratchPad) else new exit(scratchPad)
      (scratchPad, Some(nextNode))
    }
  }

  class clockGatingSettings(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""
         |set_db lp_clock_gating_infer_enable true
         |set_db lp_clock_gating_prefix {$clockGateCellPrefix}
         |set_db lp_insert_clock_gating true
         |# deprecated
         |set_db lp_clock_gating_hierarchical true
         |# deprecated
         |set_db lp_insert_clock_gating_incremental true
         |set_db lp_clock_gating_register_aware true
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      assert(waitUntil(1) { str =>
        Set(
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true"),
          str.contains(s"Setting attribute of root '/': 'lp_clock_gating_prefix' = $clockGateCellPrefix"),
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true"),
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true"),
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true")
        ).reduce(_ & _)
      }._1)
      (scratchPad, Some(new readMMMC(scratchPad)))
    }
  }

  class readMMMC(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = s"create_constraint_mode -name my_constraint_mode -sdc_files [list $clockConstrain $pinConstrain]\n" +
      corners.map(_.mmmcString).reduce(_ + "\n" + _)

    override def should: (ScratchPad, Option[ProcessNode]) = {
      assert(waitUntil(20) { str =>
        str.contains("The default library domain is")
      }._1)
      (scratchPad, Some(new readPhysical(scratchPad)))
    }
  }

  class readPhysical(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = s"read_physical -lef { ${lefs.reduce(_ + " " + _)} }"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val p = "Library has [0-9]+ usable logic and [0-9]+ usable sequential lib-cells.".r

      assert(waitUntil(20) { str =>
        p.findFirstIn(str).isDefined
      }._1)
      (scratchPad, Some(new readHdl(scratchPad)))
    }
  }

  class readHdl(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""
         |read_hdl { ${hdls.reduce(_ + " " + _)}
         |elaborate $topName
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      waitUntil(5) { str =>
        str.contains(s"design:$topName")
      }
      (scratchPad, Some(new readHdl(scratchPad)))
    }
  }

  class elaborate(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      s"""
         |init_design -top $topName
         |set_db root: .auto_ungroup none
         |set_units -capacitance 1.0pF
         |set_load_unit -picofarads 1
         |set_units -time 1.0ps
         |syn_generic
         |syn_map
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      assert(waitUntil(1) { str =>
        Set(
          str.contains("Done Elaborating Design"),
          str.contains("Done synthesizing"),
          str.contains("Done mapping")
        ).reduce(_ & _)
      }._1)
      (scratchPad, Some(new readHdl(scratchPad)))
    }
  }

  class writeDesign(var scratchPad: ScratchPad) extends ProcessNode {
    def outputSdc = workspace / topName + "_syn.sdc"

    def outputSdf = workspace / topName + "_syn.sdf"

    def outputVerilog = workspace / topName + "_syn.v"

    def input: String =
      s"""
         |set_db use_tiehilo_for_const duplicate
         |add_tieoffs -high $tie1Cell -low $tie0Cell -max_fanout 1 -verbose
         |write_hdl > $outputVerilog
         |write_sdc -view ${worstCorner.name}.${worstCorner.cornerType}_view > $outputSdc
         |write_sdf > $outputSdf
         |write_design -innovus -hierarchical -gzip_files $topName
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      assert(waitUntil(1) { str =>
        Set(
          str.contains("Done Elaborating Design"),
          str.contains("Done synthesizing"),
          str.contains("Done mapping")
        ).reduce(_ & _)
      }._1)
      scratchPad = scratchPad add
        Annotation("runtime.genus.syn_verilog", HdlPathAnnotationValue(Path(outputVerilog))) add
        Annotation("runtime.genus.syn_sdc", SdcPathAnnotationValue(Path(outputSdc))) add
        Annotation("runtime.genus.syn_sdf", SdfPathAnnotationValue(Path(outputSdf)))
      (scratchPad, Some(new readHdl(scratchPad)))
    }
  }

  class exit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "exit\n"
  }

  override val node: ProcessNode = new waitInit(scratchPadIn)


  def command: Seq[String] = {
    Seq(bin.toString)
  }
}


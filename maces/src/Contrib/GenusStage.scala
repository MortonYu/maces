package maces.contrib.cadence


import maces._
import maces.annotation._
import maces.phase._
import os._

case class GenusStage(scratchPadIn: ScratchPad) extends CliStage {

  implicit class CadenceCornerValue(c: Corner) {
    def matchLibraries(libraries: Seq[Library]): Seq[Library] = libraries.filter(l => l.voltage == c.voltage & l.temperature == c.temperature)

    def qrcFile(libraries: Seq[Library]): Path = {
      /** only one RC module */
      val f = libraries.map(_.qrcTechFile).toSet
      assert(f.size == 1)
      f.head.get
    }

    def mmmcString(libraries: Seq[Library]) =
      s"""
         |create_library_set -name ${c.name}.${c.timingType}_set -timing [list ${c.matchLibraries(libraries).map(_.toString).reduce(_ + " " + _)}]
         |create_timing_condition -name ${c.name}.${c.timingType}_cond -library_sets [list ${c.name}.${c.timingType}_set]
         |create_rc_corner -name ${c.name}.${c.timingType}_rc -temperature ${c.temperature} -qrc_tech ${qrcFile(libraries).toString}
         |create_delay_corner -name ${c.name}.${c.timingType}_delay -timing_condition ${c.name}.${c.timingType}_cond -rc_corner ${c.name}.${c.timingType}_rc
         |create_analysis_view -name ${c.name}.${c.timingType}_view -delay_corner ${c.name}.${c.timingType}_delay -constraint_mode my_constraint_mode
         |""".stripMargin
  }

  override def workspace: Path = scratchPad.get("runtime.genus.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path

  override def env: Map[String, String] = scratchPad.get("runtime.genus.env").get.asInstanceOf[EnvAnnotationValue].value

  def bin: Path = scratchPad.get("runtime.genus.bin").get.asInstanceOf[BinPathAnnotationValue].path

  def hdls: Seq[String] = scratchPad.get("runtime.genus.hdl_files").get.asInstanceOf[HdlsPathAnnotationValue].paths.map(_.toString)

  def lefs: Seq[String] = scratchPad.get("runtime.genus.tech_lef_files").get.asInstanceOf[LefsPathAnnotationValue].paths.map(_.toString)

  def topName: String = scratchPad.get("runtime.genus.top_name").get.asInstanceOf[InstanceNameAnnotationValue].value

  def coreLimit: Int = scratchPad.get("runtime.genus.core_limit").get.asInstanceOf[CoreLimitAnnotationValue].value

  def autoClockGating: Boolean = scratchPad.get("runtime.genus.auto_clock_gate").get.asInstanceOf[AutoClockGatingAnnotationValue].value

  def clockGateCellPrefix: String = scratchPad.get("runtime.genus.clock_gate_cell_prefix").get.asInstanceOf[CellNameAnnotationValue].value

  def clockConstrain: String = scratchPad.get("runtime.genus.clock_constrain_file").get.asInstanceOf[SdcPathAnnotationValue].path.toString

  def pinConstrain: String = scratchPad.get("runtime.genus.pin_constrain_file").get.asInstanceOf[SdcPathAnnotationValue].path.toString

  def tie0Cell: String = scratchPad.get("runtime.genus.tie0_cell").get.asInstanceOf[CellNameAnnotationValue].value

  def tie1Cell: String = scratchPad.get("runtime.genus.tie1_cell").get.asInstanceOf[CellNameAnnotationValue].value

  def libraries: Seq[Library] = scratchPad.get("runtime.genus.libraries").get.asInstanceOf[LibrariesAnnotationValue].value

  def corners: Seq[Corner] = scratchPad.get("runtime.genus.corners").get.asInstanceOf[CornersAnnotationValue].value

  class waitInit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = ""

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        str.contains("@genus:root: 1>")
      }
      assert(r._1, r._2)
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
      val r = waitUntil(1) { str =>
        Set(
          str.contains("Setting attribute of root '/': 'hdl_error_on_blackbox' = true"),
          str.contains(s"Setting attribute of root '/': 'max_cpus_per_server' = $coreLimit")
        ).reduce(_ & _)
      }
      assert(r._1, r._2)
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
      val r = waitUntil(1) { str =>
        Set(
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true"),
          str.contains(s"Setting attribute of root '/': 'lp_clock_gating_prefix' = $clockGateCellPrefix"),
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true"),
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true"),
          str.contains("Setting attribute of root '/': 'lp_clock_gating_infer_enable' = true")
        ).reduce(_ & _)
      }
      assert(r._1, r._2)
      (scratchPad, Some(new readMMMC(scratchPad)))
    }
  }

  class readMMMC(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = s"create_constraint_mode -name my_constraint_mode -sdc_files [list $clockConstrain $pinConstrain]\n" +
      corners.map(_.mmmcString(libraries)).reduce(_ + "\n" + _) +
      s"\nset_analysis_view -setup { ${corners.min.name}.setup_view } -hold { ${corners.max.name}.hold_view }"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val r = waitUntil(20) { str =>
        str.contains("The default library domain is")
      }
      assert(r._1, r._2)
      (scratchPad, Some(new readPhysical(scratchPad)))
    }
  }

  class readPhysical(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = s"read_physical -lef { ${lefs.reduce(_ + " " + _)} }"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      val p = "Library has [0-9]+ usable logic and [0-9]+ usable sequential lib-cells.".r

      val r = waitUntil(20) { str =>
        p.findFirstIn(str).isDefined
      }
      assert(r._1, r._2)
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
      val r = waitUntil(5) { str =>
        str.contains(s"design:$topName")
      }
      assert(r._1, r._2)
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
      val r = waitUntil(1) { str =>
        Set(
          str.contains("Done Elaborating Design"),
          str.contains("Done synthesizing"),
          str.contains("Done mapping")
        ).reduce(_ & _)
      }
      assert(r._1, r._2)
      (scratchPad, Some(new readHdl(scratchPad)))
    }
  }

  class writeDesign(var scratchPad: ScratchPad) extends ProcessNode {
    def outputSdc = workspace / topName + "_syn.sdc"

    def outputSdf = workspace / topName + "_syn.sdf"

    def outputVerilog = workspace / topName + "_syn.v"

    /** here is an implicit setting that worstCorner must be an setup corner */
    def input: String =
      s"""
         |set_db use_tiehilo_for_const duplicate
         |add_tieoffs -high $tie1Cell -low $tie0Cell -max_fanout 1 -verbose
         |write_hdl > $outputVerilog
         |write_sdc -view ${corners.min.name}.setup_view > $outputSdc
         |write_sdf > $outputSdf
         |write_design -innovus -hierarchical -gzip_files $topName
         |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      waitString(5)
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


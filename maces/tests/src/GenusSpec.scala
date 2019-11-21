package maces.tests

import ammonite.ops._
import maces._
import maces.annotation._
import maces.contrib.cadence.GenusStage
import utest._

object GenusSpec extends MacesTestSuite {
  val tests: Tests = Tests {
    test("genus should synthesis") {
      val workspace = testPath / "workspace"

      val rtlStageRunDir = workspace / "RTLStage" / System.currentTimeMillis.toString

      (new chisel3.stage.ChiselStage).run(Seq(
        new chisel3.stage.ChiselGeneratorAnnotation(() => new GCD),
        new firrtl.TargetDirAnnotation(rtlStageRunDir.toString)
      ))

      /** the runtime commercial EDA tool installation should be /opt/eda_tools/vendor/tool_name/tool,
       * license should be /opt/eda_tools/vendor/vendor.lic
       * maybe we need to provide a better way to execute this NDA-related test
       * */
      def genusBin: Path = {
        val default = Path("/opt/eda_tools/cadence/GENUS/GENUS172/bin/genus")
        if (default.isFile) default else {
          println("no default test machine, please input the genus installation path(for example: /opt/eda_tools/cadence/GENUS/GENUS172):")
          Path(scala.io.StdIn.readLine()) / "bin" / "genus"
        }
      }

      def licenseFile: Path = {
        val default = Path("/opt/eda_tools/cadence/cadence.lic")
        if (default.isFile) default else {
          println("no default test machine, please input the genus license path:")
          Path(scala.io.StdIn.readLine())
        }
      }

      def pinConstrain: Path = {
        val f = rtlStageRunDir / "pin.sdc"
        write(f, "set_load 1.0 [all_outputs]")
        f
      }

      def clockConstrain: Path = {
        val f = rtlStageRunDir / "clock.sdc"
        write(f, "")
        f
      }

      val scratchPad = ScratchPad(Set(
        Annotation("runtime.genus.stdin_shell", GeneratedFileAnnotationValue("syn.tcl")),
        Annotation("runtime.genus.enter_shell", GeneratedFileAnnotationValue("enter.sh")),
        Annotation("runtime.genus.workspace", DirectoryPathAnnotationValue(workspace)),
        Annotation("runtime.genus.bin", BinPathAnnotationValue(genusBin)),
        Annotation("runtime.genus.env", EnvAnnotationValue(Map(
          "CDS_LIC_FILE" -> licenseFile.toString,
          "OA_UNSUPPORTED_PLAT" -> "linux_rhel50_gcc48x",
          "COLUMNS" -> "1024",
          "LINES" -> "1024"
        ))),
        Annotation("runtime.genus.hdl_files", HdlsPathAnnotationValue(Seq(rtlStageRunDir / "GCD.v"))),
        Annotation("runtime.genus.tech_lef_files", LefsPathAnnotationValue(Seq(resourcesDir / "asap7" / "asap7_tech_4x_170803.lef"))),
        Annotation("runtime.genus.libraries", LibrariesAnnotationValue(asap7Libraries)),
        Annotation("runtime.genus.top_name", InstanceNameAnnotationValue("GCD")),
        Annotation("runtime.genus.core_limit", CoreLimitAnnotationValue(8)),
        Annotation("runtime.genus.auto_clock_gate", AutoClockGatingAnnotationValue(true)),
        Annotation("runtime.genus.clock_gate_cell_prefix", CellNameAnnotationValue("CLKGATE")),
        Annotation("runtime.genus.clock_constrain_file", SdcPathAnnotationValue(pinConstrain)),
        Annotation("runtime.genus.pin_constrain_file", SdcPathAnnotationValue(clockConstrain)),
        Annotation("runtime.genus.tie0_cell", CellNameAnnotationValue("TIEHIx1_ASAP7_75t_R")),
        Annotation("runtime.genus.tie1_cell", CellNameAnnotationValue("TIEHIx1_ASAP7_75t_R")),
        Annotation("runtime.genus.corners", CornersAnnotationValue(Seq(
          Corner(name = "PVT_0P63V_100C", timingType = "setup", voltage = 0.63, temperature = 100),
          Corner(name = "PVT_0P77V_0C", timingType = "hold", voltage = 0.7, temperature = 0)
        )))
      ))
      val stage = GenusStage(scratchPad)
      val scratchPadOut = stage.scratchPadOut
      val runDir = stage.runDir
      val synVerilog = runDir / "generated" / "GCD_syn.v"
      val synSdf = runDir / "generated" / "GCD_syn.sdf"
      val synSdc = runDir / "generated" / "GCD_syn.sdc"
      val stdinTcl = runDir / "generated" / "syn.tcl"
      val enter = runDir / "generated" / "enter.sh"
      assert(scratchPadOut.get("runtime.genus.syn_verilog").get == HdlPathAnnotationValue(synVerilog))
      assert(scratchPadOut.get("runtime.genus.syn_sdc").get == SdcPathAnnotationValue(synSdc))
      assert(scratchPadOut.get("runtime.genus.syn_sdf").get == SdfPathAnnotationValue(synSdf))
      assert(synVerilog.isFile)
      assert(synSdc.isFile)
      assert(synSdf.isFile)
      assert(enter.isFile)
      assert(stdinTcl.isFile)
      /** result of GENUS171*/
      assert(scratchPadOut.get("runtime.genus.cell_area").get.asInstanceOf[AreaAnnotationValue].value == 905.826)
      assert(scratchPadOut.get("runtime.genus.net_area").get.asInstanceOf[AreaAnnotationValue].value == 382.791)
      assert(scratchPadOut.get("runtime.genus.max_fanout").get.asInstanceOf[FanOutAnnotationValue].value == 67.0)
      assert(scratchPadOut.get("runtime.genus.min_fanout").get.asInstanceOf[FanOutAnnotationValue].value == 1.0)
      assert(scratchPadOut.get("runtime.genus.average_fanout").get.asInstanceOf[FanOutAnnotationValue].value == 2.1)
    }
  }
}

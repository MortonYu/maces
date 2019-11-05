package maces.tests

import ammonite.ops._
import maces._
import maces.annotation._
import maces.contrib.yosys.YosysStage
import utest._

object GenusSpecSpec extends MacesTestSuite {
  val tests: Tests = Tests {
    test("genus should synthesis") {
      val workspace = testPath / "workspace"
      rm ! testPath
      (new chisel3.stage.ChiselStage).run(Seq(
        new chisel3.stage.ChiselGeneratorAnnotation(() => new GCD),
        new firrtl.TargetDirAnnotation((workspace / "ChiselStage").toString)
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

      val scratchPad = ScratchPad(Set(
        Annotation("runtime.genus.workspace", DirectoryPathAnnotationValue(workspace)),
        Annotation("runtime.genus.bin", BinPathAnnotationValue(genusBin)),
        Annotation("runtime.genus.env", EnvAnnotationValue(Map(
          "CDS_LIC_FILE" -> licenseFile.toString,
          "OA_UNSUPPORTED_PLAT" -> "linux_rhel50_gcc48x"
        ))),
        Annotation("runtime.genus.hdl_files", HdlsPathAnnotationValue(Seq(workspace / "ChiselStage" / "GCD.v"))),
        Annotation("runtime.genus.tech_lef_files", LefsPathAnnotationValue(???)),
        Annotation("runtime.genus.liberty_cell_files", LibertyCellLibrariesPathAnnotationValue(???)),
        Annotation("runtime.genus.top_name", InstanceNameAnnotationValue("GCD")),
        Annotation("runtime.genus.core_limit", CoreLimitAnnotationValue(1)),
        Annotation("runtime.genus.auto_clock_gate", AutoClockGatingAnnotationValue(true)),
        Annotation("runtime.genus.clock_gate_cell_prefix", CellNameAnnotationValue("CLKGATE")),

        /** These annotation should be generated from another stage,
         * however, to break the dependency for test, we directly vendor it  */
        Annotation("runtime.genus.clock_constrain_file", SdcPathAnnotationValue(???)),
        Annotation("runtime.genus.pin_constrain_file", SdcPathAnnotationValue(???)),
        Annotation("runtime.genus.tie0_cell", CellNameAnnotationValue(???)),
        Annotation("runtime.genus.tie1_cell", CellNameAnnotationValue(???)),
        Annotation("runtime.genus.libraries", LibrariesAnnotationValue(???)),
        Annotation("runtime.genus.corners", LibrariesAnnotationValue(
          Seq(???)
        ))
      ))
      val stage = YosysStage(scratchPad)
      val runDir = stage.runDir
      val scratchPadOut = stage.scratchPadOut
      val synVerilog = runDir / "GCD_syn.v"
      val synSdf = runDir / "GCD_syn.sdf"
      val synSdc = runDir / "GCD_sync.sdc"
      assert(scratchPadOut.get("runtime.genus.syn_verilog").get == HdlPathAnnotationValue(synVerilog))
      assert(scratchPadOut.get("runtime.genus.syn_sdc").get == SdfPathAnnotationValue(synSdf))
      assert(scratchPadOut.get("runtime.genus.syn_sdf").get == SdcPathAnnotationValue(synSdc))
      assert(synVerilog.isFile)
      assert(synSdc.isFile)
      assert(synSdf.isFile)
    }
  }
}

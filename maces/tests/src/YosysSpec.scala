package maces.tests

import utest._
import ammonite.ops._
import maces._
import maces.annotation._
import maces.contrib.yosys.YosysStage

object YosysSpec extends MacesTestSuite {
  val tests: Tests = Tests {
    test("yosys should give a opt verilog") {
      val workspace = testPath / "workspace"
      rm ! testPath
      (new chisel3.stage.ChiselStage).run(Seq(
        new chisel3.stage.ChiselGeneratorAnnotation(() => new GCD),
        new firrtl.TargetDirAnnotation((workspace / "ChiselStage").toString)
      ))

      val scratchPad = ScratchPad(Set(
        Annotation("user.input.verilogs", HdlsPathAnnotationValue(Seq(workspace / "ChiselStage" / "GCD.v"))),
        Annotation("user.vendor.liberty_cell_libraries", LibertyCellLibrariesPathAnnotationValue(Seq(resourcesDir / "asap7sc7p5t_24_SIMPLE_RVT_TT.lib"))),
        Annotation("user.input.top", InstanceNameAnnotationValue("GCD")),
        Annotation("user.bin.yosys", BinPathAnnotationValue(Path("/usr/bin/yosys"))),
        Annotation("system.workspace", DirectoryPathAnnotationValue(workspace))
      ))
      val stage = YosysStage(scratchPad)
      val runDir = stage.runDir
      val scratchPadOut = stage.scratchPadOut
      val optVerilog = runDir / "GCD_opt.v"
      assert(scratchPadOut.get("runtime.yosys.opt_verilog").get == HdlsPathAnnotationValue(Seq(optVerilog)))
      assert(optVerilog.isFile)
    }
  }
}

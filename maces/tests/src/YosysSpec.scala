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
        Annotation("runtime.yosys.verilogs", HdlsPathAnnotationValue(Seq(workspace / "ChiselStage" / "GCD.v"))),
        Annotation("runtime.yosys.liberty_cell_paths", LibertyCellLibrariesPathAnnotationValue(Seq(resourcesDir / "asap7sc7p5t_24_SIMPLE_RVT_TT.lib"))),
        Annotation("runtime.yosys.top", InstanceNameAnnotationValue("GCD")),
        Annotation("runtime.yosys.bin", BinPathAnnotationValue(Path("/usr/bin/yosys"))),
        Annotation("runtime.yosys.workspace", DirectoryPathAnnotationValue(workspace))
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

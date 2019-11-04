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

      val scratchPad = ScratchPad(Set(
        Annotation("runtime.genus.workspace", DirectoryPathAnnotationValue(workspace)),
        Annotation("runtime.genus.bin", BinPathAnnotationValue(Path("/usr/bin/yosys"))),
        Annotation("runtime.genus.env", ???),
        Annotation("runtime.genus.hdl_files", ???),
        Annotation("runtime.genus.lef_files", ???),
        Annotation("runtime.genus.liberty_cell_files", ???),
        Annotation("runtime.genus.top_name", ???),
        Annotation("runtime.genus.core_limit", ???),
        Annotation("runtime.genus.auto_clock_gate", ???),
        Annotation("runtime.genus.clock_gate_cell_prefix", ???),
        Annotation("runtime.genus.clock_constrain_file", ???),
        Annotation("runtime.genus.pin_constrain_file", ???),
        Annotation("runtime.genus.tie0_cell", ???),
        Annotation("runtime.genus.tie1_cell", ???),
        Annotation("runtime.genus.mmmc_corners", ???)
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

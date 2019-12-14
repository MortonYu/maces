package maces.tests

import maces.annotation._

import ammonite.ops._
import utest._
import utest.framework.Formatter
import chisel3._

abstract class MacesTestSuite extends TestSuite {
  def sanitizePath(path: String): String = path.toString.replaceAll(" ", "_").replaceAll("\\W+", "")

  def testPath()(implicit testPath: utest.framework.TestPath) = Path(s"test_run_dir/${this.getClass.getSimpleName.dropRight(1)}/" + sanitizePath(testPath.value.reduce(_ + _)), pwd)

  def resourcesDir = Path(getClass.getResource("/").getPath)

  override def utestFormatter: Formatter = new Formatter {
    override def exceptionStackFrameHighlighter(s: StackTraceElement): Boolean = {
      Set("maces").map(s.getClassName.startsWith(_)).reduce(_ && _)
    }
  }

  class GCD extends MultiIOModule {
    val a: UInt = IO(Input(UInt(32.W)))
    val b: UInt = IO(Input(UInt(32.W)))
    val e: Bool = IO(Input(Bool()))
    val z: UInt = IO(Output(UInt(32.W)))
    val v: Bool = IO(Output(Bool()))
    val x: UInt = Reg(UInt(32.W))
    val y: UInt = Reg(UInt(32.W))
    when(x > y) {
      x := x -% y
    }.otherwise {
      y := y -% x
    }
    when(e) {
      x := a
      y := b
    }
    z := x
    v := y === 0.U
  }

  def asap7Libraries = Seq(
    Library(name = "asap7_ao_rvt_ss", voltage = 0.63, temperature = 100, nominalType = "ss",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_AO_RVT_SS.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_ao_rvt_tt", voltage = 0.7, temperature = 25, nominalType = "tt",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_AO_RVT_TT.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_ao_rvt_ff", voltage = 0.7, temperature = 0, nominalType = "ff",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_AO_RVT_FF.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_invbuf_rvt_ss", voltage = 0.63, temperature = 100, nominalType = "ss",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_INVBUF_RVT_SS.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_invbuf_rvt_tt", voltage = 0.7, temperature = 25, nominalType = "tt",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_INVBUF_RVT_TT.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_invbuf_rvt_ff", voltage = 0.7, temperature = 0, nominalType = "ff",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_INVBUF_RVT_FF.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_oa_rvt_ss", voltage = 0.63, temperature = 100, nominalType = "ss",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_OA_RVT_SS.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_oa_rvt_tt", voltage = 0.7, temperature = 25, nominalType = "tt",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_OA_RVT_TT.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_oa_rvt_ff", voltage = 0.7, temperature = 0, nominalType = "ff",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_OA_RVT_FF.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_seq_rvt_ss", voltage = 0.63, temperature = 100, nominalType = "ss",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_SEQ_RVT_SS.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_seq_rvt_tt", voltage = 0.7, temperature = 25, nominalType = "tt",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_SEQ_RVT_TT.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_seq_rvt_ff", voltage = 0.7, temperature = 0, nominalType = "ff",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_SEQ_RVT_FF.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_simple_rvt_ss", voltage = 0.63, temperature = 100, nominalType = "ss",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_SIMPLE_RVT_SS.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_simple_rvt_tt", voltage = 0.7, temperature = 25, nominalType = "tt",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_SIMPLE_RVT_TT.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds")),
    Library(name = "asap7_simple_rvt_ff", voltage = 0.7, temperature = 0, nominalType = "ff",
      libertyFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_SIMPLE_RVT_FF.lib"),
      qrcTechFile = Some(resourcesDir / "asap7" / "qrcTechFile_typ03_scaled4xV06"),
      lefFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R_4x_170912.lef"),
      spiceFile = Some(resourcesDir / "asap7" / "asap7_75t_R.cdl"),
      gdsFile = Some(resourcesDir / "asap7" / "asap7sc7p5t_24_R.gds"))
  )
}

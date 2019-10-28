package maces.tests

import ammonite.ops._
import utest._
import utest.framework.Formatter
import chisel3._

abstract class MacesTestSuite extends TestSuite {
  def sanitizePath(path: String): String = path.toString.replaceAll(" ", "_").replaceAll("\\W+", "")

  def testPath()(implicit testPath: utest.framework.TestPath) = Path(s"test_run_dir/${this.getClass.getSimpleName.dropRight(1)}/" + sanitizePath(testPath.value.reduce(_ + _)), pwd)

  def resourcesDir = Path("maces/tests/resources", pwd)

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

}
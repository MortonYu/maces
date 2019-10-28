package maces.tests

import utest._
import chisel3._

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

object YosysSpec extends MacesTestSuite {
  val tests: Tests = Tests {
    test("emit gcd") {
      (new chisel3.stage.ChiselStage).run(Seq(
        new chisel3.stage.ChiselGeneratorAnnotation(() => new GCD),
        new firrtl.TargetDirAnnotation(testPath.toString)
      ))
    }
  }
}

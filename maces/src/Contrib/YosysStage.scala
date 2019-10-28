package maces.contrib.yosys


import maces._
import maces.annotation._
import maces.phase._
import os._
import os.SubProcess._
//
//case class DemoPythonWrapperStage(scratchPadIn: ScratchPad) extends CliStage {
//
//  class readVerilog(var scratchPad: ScratchPad) extends ProcessNode {
//    def input: String = "\n"
//
//    override def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = {
//      scratchPad = scratchPad add Annotation("runtime.demo_python_wrapper.pn0_result", DemoStringResultAnnotation(stdout.readLine))
//      (scratchPad, Some(new pn1(scratchPad)))
//    }
//  }
//
//  class pn1(var scratchPad: ScratchPad) extends ProcessNode {
//    def input: String =
//      """import os
//        |print(os.environ["someEnv0"])
//        |""".stripMargin
//
//    override def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = {
//      (scratchPad add Annotation("runtime.demo_python_wrapper.pn1_result", DemoStringResultAnnotation(stdout.readLine)), None)
//    }
//  }
//
//  override val node: ProcessNode = new pn0(scratchPadIn)
//
//  def env: Map[String, String] = Map(
//    "someEnv0" -> "1"
//  )
//
//  def bin: Path = scratchPad.get("user.bin.python3").get.asInstanceOf[BinPathAnnotationValue].path
//
//  def command: Seq[String] = {
//    Seq(bin.toString, "-u", "-c", "while True: exec(input())")
//  }
//}
//

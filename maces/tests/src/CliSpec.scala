package maces.tests

import maces._
import maces.annotation._
import maces.phase._
import os._
import os.SubProcess._
import utest._

case class DemoStringResultAnnotation(str: String) extends AnnotationValue

case class DemoPythonWrapperStage(scratchPadIn: ScratchPad) extends CliStage {

  class pn0(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "print(1+1)\n"

    override def should: (ScratchPad, Option[ProcessNode]) = {
      scratchPad = scratchPad add Annotation("runtime.demo_python_wrapper.pn0_result", DemoStringResultAnnotation(waitString(1).dropRight(1)))
      (scratchPad, Some(new pn1(scratchPad)))
    }
  }

  class pn1(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String =
      """import os
        |print(os.environ["someEnv0"])
        |""".stripMargin

    override def should: (ScratchPad, Option[ProcessNode]) = {
      (scratchPad add Annotation("runtime.demo_python_wrapper.pn1_result", DemoStringResultAnnotation(waitString(1).dropRight(1))), None)
    }
  }

  override val node: ProcessNode = new pn0(scratchPadIn)

  override def env: Map[String, String] = Map(
    "someEnv0" -> "1"
  )

  def bin: Path = scratchPad.get("user.bin.python3").get.asInstanceOf[BinPathAnnotationValue].path

  def command: Seq[String] = {
    Seq(bin.toString, "-u", "-c", "while True: exec(input())")
  }

  override def workspace: Path = scratchPad.get("runtime.test.workspace").get.asInstanceOf[DirectoryPathAnnotationValue].path
}

object CliSpec extends MacesTestSuite {
  val tests: Tests = Tests {
    test("some test") {
      val scratchPad = ScratchPad(Set(
        Annotation("runtime.test.workspace", DirectoryPathAnnotationValue(testPath)),
        Annotation("user.bin.python3", BinPathAnnotationValue(Path(proc("which", "python3").call().out.string.dropRight(1))))
      ))
      val scratchPadOut = DemoPythonWrapperStage(scratchPad).scratchPadOut
      assert(scratchPadOut.get("runtime.demo_python_wrapper.pn0_result").get == DemoStringResultAnnotation("2"))
      assert(scratchPadOut.get("runtime.demo_python_wrapper.pn1_result").get == DemoStringResultAnnotation("1"))
    }
  }
}

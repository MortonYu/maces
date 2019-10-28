package maces.contrib.yosys


import maces._
import maces.annotation._
import maces.phase._
import os._
import os.SubProcess._

case class YosysStage(scratchPadIn: ScratchPad) extends CliStage {
  def verilogs: Seq[String] = scratchPad.get("user.input.verilogs").get.asInstanceOf[VerilogsPathAnnotationValue].paths.map(_.toString)

  def libertyLibraries: Seq[String] = scratchPad.get("user.vendor.liberty_cell_libraries").get.asInstanceOf[LibertyCellLibrariesPathAnnotationValue].paths.map(_.toString)

  def topName: String = scratchPad.get("user.input.top").get.asInstanceOf[InstanceNameAnnotationValue].value

  class init(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "\n"

    override def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = {
      waitLineWithFilter { str =>
        str.contains("yosys>")
      }
      (scratchPad, Some(new readVerilog(scratchPad)))
    }
  }

  class readVerilog(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "read_verilog " + verilogs.reduce(_ + " " + _) + "\n"

    override def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = {
      waitLineWithFilter { str =>
        str.contains("Successfully")
      }
      (scratchPad, Some(new readLibertyLibrary(scratchPad)))
    }
  }

  class readLibertyLibrary(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "read_liberty " + libertyLibraries.reduce(_ + " " + _) + "\n"

    override def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = {
      waitLineWithFilter { str =>
        str.contains("Imported")
      }
      (scratchPad, Some(new hierarchyCheckAndOpt(scratchPad)))
    }
  }

  class hierarchyCheckAndOpt(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = s"hierarchy -check -top $topName; proc; opt; fsm; opt; memory; opt; techmap; opt;\n"

    override def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = {
      /** There are 4 opt passes. */
      Seq.tabulate(4) {
        waitLineWithFilter { str =>
          str.contains("Finished OPT passes.")
        }
      }
      (scratchPad, Some(new writeVerilog(scratchPad)))
    }
  }

  class writeVerilog(var scratchPad: ScratchPad) extends ProcessNode {
    val outOptVerilog = Path(topName + "_opt.v", runDir)

    def input: String = s"write_verilog $outOptVerilog\n"


    override def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = {
      waitLineWithFilter { str =>
        str.contains("Dumping module")
      }
      scratchPad = scratchPad add Annotation("runtime.yosys.opt_verilog", VerilogsPathAnnotationValue(Seq(outOptVerilog)))
      (scratchPad, Some(new exit(scratchPad)))
    }
  }

  class exit(var scratchPad: ScratchPad) extends ProcessNode {
    def input: String = "exit\n"
  }

  override val node: ProcessNode = new init(scratchPadIn)

  def bin: Path = scratchPad.get("user.bin.yosys").get.asInstanceOf[BinPathAnnotationValue].path

  def command: Seq[String] = {
    Seq(bin.toString)
  }
}


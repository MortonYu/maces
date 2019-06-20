package maces.compiler

import firrtl.annotations.{Annotation, NoTargetAnnotation}
import firrtl.options._
import firrtl.stage._

object FirrtlFileInOption extends HasShellOptions {
  val options = Seq(
    new ShellOption[String](
      longOption = "firrtl-in",
      toAnnotationSeq = a => Seq(FirrtlFileAnnotation(a)),
      helpText = "An input FIRRTL file",
      shortOption = None,
      helpValueName = Some("<file>"))
  )
}

object FirrtlFileOutOption extends HasShellOptions {
  val options = Seq(
    new ShellOption[String](
      longOption = "firrtl-out",
      toAnnotationSeq = a => Seq(FirrtlFileAnnotation(a)),
      helpText = "An output FIRRTL file",
      shortOption = None,
      helpValueName = Some("<file>"))
  )
}

object VerilogFileInOption extends HasShellOptions {
  val options = Seq(
    new ShellOption[String](
      longOption = "verilog-in",
      toAnnotationSeq = a => Seq(FirrtlFileAnnotation(a)),
      helpText = "An input Verilog file",
      shortOption = None,
      helpValueName = Some("<file>"))
  )
}

object VerilogFileOutOption extends HasShellOptions {
  val options = Seq(
    new ShellOption[String](
      longOption = "verilog-out",
      toAnnotationSeq = a => Seq(FirrtlFileAnnotation(a)),
      helpText = "An output Verilog file",
      shortOption = None,
      helpValueName = Some("<file>"))
  )
}

object SDCFileInOption extends HasShellOptions {
  val options = Seq(
    new ShellOption[String](
      longOption = "input-sdc-file",
      toAnnotationSeq = a => Seq(FirrtlFileAnnotation(a)),
      helpText = "An input FIRRTL file",
      shortOption = Some("i"),
      helpValueName = Some("<file>"))
  )
}

object SDCFileOutOption extends HasShellOptions {
  val options = Seq(
    new ShellOption[String](
      longOption = "input-sdc-file",
      toAnnotationSeq = a => Seq(FirrtlFileAnnotation(a)),
      helpText = "An input FIRRTL file",
      shortOption = Some("i"),
      helpValueName = Some("<file>"))
  )
}


sealed trait MacesOption {
  this: Annotation =>
}

case class MacesCompilerAnnotation(compiler: MacesCompiler = new MacesCompiler()) extends NoTargetAnnotation with MacesOption

object CompilerAnnotation extends HasShellOptions {
  def apply(compilerName: String): MacesCompilerAnnotation = {
    val c = compilerName match {
      case "timing" => new NoneMacesCompiler()
      case "techmap" => new NoneMacesCompiler()
      case "synthesis" => new NoneMacesCompiler()
      case "place" => new NoneMacesCompiler()
      case "route" => new NoneMacesCompiler()
      case "power" => new NoneMacesCompiler()
      case "simulation" => new NoneMacesCompiler()
      case _ => throw new OptionsException(s"Unknown compiler name '$compilerName'! (Did you misspell it?)")
    }
    MacesCompilerAnnotation(c)
  }

  val options = Seq(
    new ShellOption[String](
      longOption = "compiler",
      toAnnotationSeq = a => Seq(CompilerAnnotation(a)),
      helpText = "The FIRRTL compiler to use (default: verilog)",
      shortOption = Some("X"),
      helpValueName = Some("<none|high|middle|low|verilog|mverilog|sverilog>")))

}

trait MacesCli {
  this: Shell =>
  parser.note("Maces Compiler Options")
  Seq(
    FirrtlFileInOption,
    FirrtlFileOutOption,
    VerilogFileInOption,
    VerilogFileOutOption,
    CompilerAnnotation
  ).foreach(_.addOptions(parser))
}


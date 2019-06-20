package maces.compiler

import firrtl.ir.Circuit
import firrtl.options.OptionsView
import firrtl.{AnnotationSeq, ChirrtlEmitter, ChirrtlForm, Transform}
import firrtl.stage.phases.Compiler
import firrtl.transforms.IdentityTransform

class MacesOptions private[maces](val nailOptions: Option[String] = None,
                                  val additionalInputFileName: Option[String] = None,
                                  val outputFileName: Option[String] = None,
                                  val compiler: MacesCompiler = MacesCompilerAnnotation().compiler,
                                  val firrtlCircuit: Option[Circuit] = None)

class MacesOptionsView extends OptionsView[MacesOptions] {
  override def view(options: AnnotationSeq): MacesOptions = ???
}

class NoneMacesCompiler extends MacesCompiler {
  val emitter = new ChirrtlEmitter
  def transforms: Seq[Transform] = Seq(new IdentityTransform(ChirrtlForm))
}

class MacesCompiler extends Compiler {

}

class DebugCompiler extends MacesCompiler {

}

package maces.annotations

import chisel3._
import chisel3.experimental._
import firrtl.annotations.{ModuleTarget, SingleTargetAnnotation}

// Annotation for chisel
object annoRetiming {
  /** Marks a module need Retiming in synthesis
    *
    * @param module The module to be marked Retiming
    * @return Unmodified `module`
    */
  def apply[T <: RawModule](module: T)(implicit compileOptions: CompileOptions): Unit = {
    // TODO: PR chisel3 make `toNamed` deprecated, use `toTarget` instead
    annotate(new ChiselAnnotation {
      def toFirrtl = RetimingAnnotation(module.toNamed.toTarget)
    })
  }
}

// Annotation for firrtl
case class RetimingAnnotation(target: ModuleTarget) extends SingleTargetAnnotation[ModuleTarget] {
  def duplicate(n: ModuleTarget): RetimingAnnotation = RetimingAnnotation(n)
}


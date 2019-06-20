package maces

import firrtl._
import firrtl.options._
import firrtl.options.phases._
import firrtl.passes._
import maces.compiler._
import scala.util.control.ControlThrowable

case class MacesException(cause: Throwable) extends Exception("", cause)

class MacesStage extends Stage {
  val shell: Shell = new Shell("maces") with MacesCli

  private val phases: Seq[Phase] =
    Seq(
      new maces.phases.Check,
      new maces.phases.Probe,
      new maces.phases.Compile,
      new maces.phases.Emit,
      new maces.phases.Schedule,
      new maces.phases.Submit
    ).map(DeletedWrapper(_))

  def run(annotations: AnnotationSeq): AnnotationSeq =
    try {
      phases.foldLeft(annotations)((a, f) => f.transform(a))
    } catch {
      /* Rethrow the exceptions which are expected or due to the runtime environment (out of memory, stack overflow, etc.).
       * Any UNEXPECTED exceptions should be treated as internal errors. */
      // Maces should connect to EDA server behind Nail
      case MacesException(cause)           => throw cause
      case CustomTransformException(cause) => throw cause
      case p @ (_: ControlThrowable | _: PassException | _: PassExceptions |
          _: FIRRTLException | _: OptionsException | _: PhaseException) =>
        throw p
      case e: Exception => Utils.throwInternalError(exception = Some(e))
    }
}

object MacesMain extends StageMain(new MacesStage)

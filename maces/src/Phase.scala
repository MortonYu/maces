package maces

trait Phase {
  lazy val name: String = this.getClass.getName

  val scratchPadIn: ScratchPad

  implicit var scratchPad: ScratchPad = scratchPadIn

  def prePhase: Seq[ScratchPad => Phase] = Seq()

  def transform: ScratchPad

  def postPhase: Seq[ScratchPad => Phase] = Seq()

  def scratchPadOut: ScratchPad = {
    /** Although 1L is easy to write here, I choose a clear way to express it. */
    /** prePhase */
    scratchPad = if (prePhase.nonEmpty) prePhase.foldLeft(scratchPad)((s, p) => p(s).scratchPadOut) else scratchPad

    /** transform */
    scratchPad = transform

    /** postPhase */
    scratchPad = if (postPhase.nonEmpty) postPhase.foldLeft(scratchPad)((s, p) => p(s).scratchPadOut) else scratchPad
    scratchPad
  }
}

trait Stage extends Phase {
  def phases: Seq[ScratchPad => Phase]

  def transform: ScratchPad = phases.foldLeft(scratchPad)((s, p) => p(s).scratchPadOut)
}

case class IdentityPhase(scratchPadIn: ScratchPad) extends Phase {
  def transform: ScratchPad = scratchPadIn
}

abstract case class FilterPhase(scratchPadIn: ScratchPad) extends Phase {
  def filterFunc(annotation: Annotation): Boolean

  def transform(scratchPad: ScratchPad): ScratchPad = ScratchPad(scratchPadIn.annotations.filter(filterFunc))
}

abstract case class AppendPhase(scratchPadIn: ScratchPad) extends Phase {
  def appendFunc: Annotation

  def transform(scratchPad: ScratchPad): ScratchPad = scratchPadIn add appendFunc
}

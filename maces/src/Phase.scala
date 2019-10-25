package maces

trait Phase {
  lazy val name: String = this.getClass.getName

  val scratchPadIn: ScratchPad

  def prePhase: Seq[ScratchPad => Phase] = Seq()

  def transform(scratchPad: ScratchPad): ScratchPad

  def postPhase: Seq[ScratchPad => Phase] = Seq()

  def scratchPadOut: ScratchPad = {
    /** Although 1L is easy to write here, I choose a clear way to express it. */
    val scratchPadAfterPrePhase = if (prePhase.nonEmpty) prePhase.foldLeft(scratchPadIn)((s, p) => p(s).scratchPadOut) else scratchPadIn
    val scratchPadAfterTransform = transform(scratchPadAfterPrePhase)
    val scratchPadAfterPostPhase = if (postPhase.nonEmpty) postPhase.foldLeft(scratchPadAfterTransform)((s, p) => p(s).scratchPadOut) else scratchPadAfterTransform
    scratchPadAfterPostPhase
  }
}

case class IdentityPhase(scratchPadIn: ScratchPad) extends Phase {
  def transform(scratchPad: ScratchPad): ScratchPad = scratchPadIn
}

abstract case class FilterPhase(scratchPadIn: ScratchPad) extends Phase {
  def filterFunc(annotation: Annotation): Boolean

  def transform(scratchPad: ScratchPad): ScratchPad = ScratchPad(scratchPadIn.annotations.filter(filterFunc))
}

abstract case class AppendPhase(scratchPadIn: ScratchPad) extends Phase {
  def appendFunc: Annotation

  def transform(scratchPad: ScratchPad): ScratchPad = scratchPadIn add appendFunc
}

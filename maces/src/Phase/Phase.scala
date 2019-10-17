package maces.phase

import maces.annotation._

trait Phase {
  lazy val name: String = this.getClass.getName

  val scratchPadIn: ScratchPad

  def prePhase: Seq[Phase] = Seq()

  def transform(scratchPad: ScratchPad): ScratchPad

  def postPhase: Seq[Phase] = Seq()

  final def run(scratchPad: ScratchPad): ScratchPad = postPhase.foldLeft(transform(prePhase.foldLeft(scratchPadIn)((s, p) => p.run(s))))((s, p) => p.run(s))
}
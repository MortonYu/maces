package maces.phase

import maces.annotation.ScratchPad

import annotation._
trait Phase {
  val scratchPad: ScratchPad
  val prevPhase: Phase
  def transform(anno: Set[Annotation]): Set[Annotation]
  val postPhase: Phase
}


package maces.annotation

import ammonite.ops._

trait FileAnnotation extends AnnotationValue {
  val path: Path
}

case class NormalFileAnnotation(path: Path) extends FileAnnotation

package maces.annotation

case class AnnotationHierarchy(path: Seq[String]) {
  /**TODO: add wildcard here for better match,
   * currently only support entire equality check.
   * maybe reinvent NFA later
   * */
  def is(that: AnnotationHierarchy): Boolean = {
    this == that
  }
}

object AnnotationHierarchy {
  /** use cadence.genus.multiCore for easy match. */
  implicit def str2ah(path: String): AnnotationHierarchy = AnnotationHierarchy(path.split('.'))
}

trait AnnotationValue

/** [[Annotation]] is a [[String]] -> [[AnnotationValue]],
 *
 * any values should be read from [[AnnotationValue]]
 * jsonschema should never be apply in maces,
 * since developers should understand what they should write and read,
 * user can extends from [[AnnotationValue]] for value extraction,
 * for example Genus tool can be added by:
 * {{{
 *   case class Threads(n: Int) extends AnnotationValue
 *   Annotation(AnnotationHierarchy("cadence", "genus", "multiCore"), Threads(n))
 * }}}
 *
 * */

case class Annotation(key: AnnotationHierarchy, value: AnnotationValue)

/** [[ScratchPad]] is used for store [[Annotation]], organize, find, add */
case class ScratchPad(annotations: Set[Annotation]) {
  def add(that: Set[Annotation]): ScratchPad = ScratchPad(annotations ++ that)

  def add(that: Annotation): ScratchPad = add(Set(that))

  def add(key: AnnotationHierarchy, value: AnnotationValue): ScratchPad = add(Annotation(key, value))

  def delete(key: AnnotationHierarchy): ScratchPad = ScratchPad(annotations.diff(find(key)))

  def find(key: AnnotationHierarchy): Set[Annotation] = annotations.filter(annotation => annotation.key.is(key))

  def get(key: AnnotationHierarchy): Option[Annotation] = {
    val findResult = find(key)
    findResult.size match {
      case 1 => Some(findResult.head)
      case 0 => None
      case _ =>
        /** TODO: add warning after finish logger framework*/
        print(s"[EE] $key matches multi annotations")
        None
    }
  }

  def update(key: AnnotationHierarchy, anno: Annotation): ScratchPad = {
    val findResult = find(key)
    findResult.size match {
      case 1 => delete(findResult.head.key).add(anno)
      case 0 =>
        /** TODO: add warning after finish logger framework*/
        print("[WW] update $key matches none annotations")
        add(anno)
      case _ =>
        print(s"[EE] update $key matches multi annotations")
        this
    }
  }
}
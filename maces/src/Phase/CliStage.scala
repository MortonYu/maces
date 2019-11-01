package maces.phase

import maces._
import maces.annotation._
import os.SubProcess._
import os._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ProcessNode {
  var scratchPad: ScratchPad

  def input: String

  def should: (ScratchPad, Option[ProcessNode]) = (scratchPad, None)
}

trait CliStage extends Phase with HasWorkspace {
  var scratchPad: ScratchPad

  val node: ProcessNode

  def env: Map[String, String] = Map()

  def command: Seq[String]

  /** TODO: still have system env, seems to be a bug? */
  val sub: SubProcess = {
    val process = os.proc(command).spawn(
      cwd = runDir,
      env = env,
      propagateEnv = false
    )

    /** invoke a async function to read stdout to [[stdout]] */
    var b: Char = 0
    Future(do {
      b = sub.stdout.read.toChar
      stdout.append(b)
    } while (b != -1))

    process
  }

  def stdin: SubProcess.InputStream = sub.stdin

  val stdout = new StringBuilder

  def waitUntil(timeout: Int)(filter: String => Boolean): (Boolean, String) = {
    val startTime = System.currentTimeMillis
    while (System.currentTimeMillis - startTime < timeout * 1000) {
      val currentStdout = stdout.toString
      if (filter(currentStdout)) {
        stdout.clear()
        return (true, currentStdout)
      }
    }
    val currentStdout = stdout.toString
    stdout.clear()
    (false, currentStdout)
  }

  def waitString(timeout: Int): String = waitUntil(timeout)(_ => false)._2

  def stderr: SubProcess.OutputStream = sub.stderr

  lazy val runDir: Path = {
    val dir = workspace / this.getClass.getSimpleName / System.currentTimeMillis.toString
    os.makeDir.all(dir)
    dir
  }

  def runHelper(pn: ProcessNode): Option[ProcessNode] = {
    stdin.write(pn.input)
    stdin.flush()
    val scratchPadAndNextPn = pn.should
    scratchPad = scratchPadAndNextPn._1
    val nextPn = scratchPadAndNextPn._2
    if (nextPn.isDefined) runHelper(nextPn.get) else None
  }

  def transform: ScratchPad = {
    runHelper(node)
    sub.destroy()
    scratchPad
  }
}

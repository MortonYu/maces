package maces.phase

import maces._
import maces.annotation._
import os.SubProcess._
import os._


abstract class ProcessNode {
  var scratchPad: ScratchPad

  def input: String

  def should(stdout: OutputStream): (ScratchPad, Option[ProcessNode]) = (scratchPad, None)
}

trait CliStage extends Phase with HasWorkspace {
  var scratchPad: ScratchPad

  val node: ProcessNode

  def env: Map[String, String] = Map()

  def command: Seq[String]

  /** TODO: still have system env, seems to be a bug? */
  val sub: SubProcess = {
    os.proc(command).spawn(
      cwd = runDir,
      env = env,
      propagateEnv = false
    )
  }

  def stdin: SubProcess.InputStream = sub.stdin

  def stdout: SubProcess.OutputStream = sub.stdout

  def waitLineWithFilter(filter: String => Boolean): String = {
    val currentOutput = stdout.readLine()
    if(filter(currentOutput)) currentOutput else waitLineWithFilter(filter)
  }

  def stderr: SubProcess.OutputStream = sub.stderr

  lazy val runDir: Path = {
    val dir = workspace / this.getClass.getSimpleName / System.currentTimeMillis.toString
    os.makeDir.all(dir)
    dir
  }

  def runHelper(pn: ProcessNode): Option[ProcessNode] = {
    stdin.write(pn.input)
    stdin.flush()
    val scratchPadAndNextPn = pn.should(stdout)
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

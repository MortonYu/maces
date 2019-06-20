import chisel3._
import firrtl.FirrtlExecutionSuccess
import org.scalatest.{FreeSpec, Matchers}
import maces.annotations.annoRetiming

class Counter extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(16.W))
    val out = Output(UInt(16.W))
  })
  io.out := io.in +% 1.U
}

class Counter4(addAnnos: Boolean) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(16.W))
    val out = Output(UInt(16.W))
  })

  val c0 = Module(new Counter)
  val c1 = Module(new Counter)
  val c2 = Module(new Counter)
  val c3 = Module(new Counter)

  c0.io.in := io.in
  c1.io.in := c0.io.out
  c2.io.in := c1.io.out
  c3.io.in := c2.io.out
  io.out := c3.io.out

  if (addAnnos) {
    annoRetiming(c1)
    annoRetiming(c3)
  }
}

class AnnotationRetiming extends FreeSpec with Matchers {
  "Annotations can be added which will prevent this deduplication for specific modules instances" in {
    Driver.execute(
      Array("-X", "low", "--target-dir", "test_run_dir"),
      () => new Counter4(addAnnos = true)
    ) match {
      case ChiselExecutionSuccess(
          _,
          _,
          Some(firrtlResult: FirrtlExecutionSuccess)
          ) =>
        val lowFirrtl = firrtlResult.emitted
        print(lowFirrtl)
      case _ =>
    }
  }
}

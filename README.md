# Maces(Mask Aimed Chisel Expediting System)
This project is inspired by [hammer](https://github.com/ucb-bar/hammer), rewrite Hammer IR transform by Scala as a firrtl Stage.
These transforms deliver physics information by Chisel/Firrtl Annotation, including
1. Timing Constrain
2. Power Constrain
3. Place and Route Constrain
4. Simulation Probe
5. EDA Tool Selection

These annotations will be transformed by `Firrtl.Transform` and finally emit to tcl for EDA tools.
These TCL and source will be send to NAIL server by NAIL protocol, consumed by NAIL EDA Plug-In.

## Hammer IR(unofficial version)
### Set Clock
```
annoClock(clock: ReferenceTarget, sourceName: String, period: Option[Double], waveform: Waveform, father: Option[Product[ReferenceTarget, multi: Double] = None, phase: Double = 0, jitter: Double = 0)
case class Waveform(risingTransition: Double, fallingTransition: Double)
```
need to check `clock` is `ClockType`.

Waveform is clock info for rising and falling time.
> The rising and falling edge times of the waveform of the defined clock, in nanoseconds, over one full clock cycle. You can use multiple rising and falling edges to define the characteristics of the waverform, but there must be an even number of edges, representing both the rising and falling edges of the waveform.
The first time specified (arg1) represents the time of the first rising transition, and the second time specified (arg2) is the falling edge. If the value for the falling edge is smaller than the value for the rising edge, it means that the falling edge occurs before the rising edge.
> Note: If you do not specify the waveform, the default waveform is assumedto have a rising edge at time 0.0 and a falling edge at one half the specified period (-period/2).
> The following example creates a clock named clk on the input port, bftClk, with a period of 10ns, the rising edge at 2.4ns and the falling edge at 7.4ns:
> create_clock -name clk -period 10.000 -waveform {2.4 7.4}

father is upstream Clock, multi is relationship between 2 clock.

phase is clock phase, default is 0.

We need to check what information can be provided by clock-infer branch.

### Clock Exception
```
annoFalsePath()
```

```
annoMultiCycle()
```

```
annoMaxDelay()
```

```
annoMinDelay()
```

```
annoDisableClock()
```

```
annoRetiming()
```

3. Set CDC
```
annoClockDomainCrossing()
```

4. Set Delay
```
SetDelay(node: ReferenceTarget, delay: Double)
```
need check `node` is `Port` of top module.


### Placement

### Routing

### Simulation

## Connect to NAIL
Multi NAIL Server can be put into the MACES pool.
In each Stage of `MACES`, it will check all NAIL return weight, and use the highest weight NAIL server.

Stage will become thenn

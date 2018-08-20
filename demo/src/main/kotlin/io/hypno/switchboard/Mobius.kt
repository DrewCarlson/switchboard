package io.hypno.switchboard

import com.spotify.mobius.*
import com.spotify.mobius.Next.*
import com.spotify.mobius.functions.Consumer

data class CounterModel(val count: Int = 0) {
  operator fun plus(number: Int) = copy(count = count + number)
  operator fun minus(number: Int) = copy(count = count - number)
}

@MobiusUpdateSpec(
    prefix = "Counter",
    baseModel = CounterModel::class)
sealed class MobiusCounterEvent {
  object Inc : MobiusCounterEvent()
  object Dec : MobiusCounterEvent()
  data class Add(val number: Int) : MobiusCounterEvent()
  data class Sub(val number: Int) : MobiusCounterEvent()
}

class MobiusUpdateFunc : Update<CounterModel, MobiusCounterEvent, Any>, CounterUpdateSpec {
  override fun update(model: CounterModel, event: MobiusCounterEvent) = patch(model, event)

  override fun inc(model: CounterModel): Next<CounterModel, Any> = next(model + 1)
  override fun dec(model: CounterModel): Next<CounterModel, Any> = next(model - 1)

  override fun add(model: CounterModel, event: MobiusCounterEvent.Add): Next<CounterModel, Any> =
      next(model + event.number)

  override fun sub(model: CounterModel, event: MobiusCounterEvent.Sub): Next<CounterModel, Any> =
      next(model - event.number)
}

class EffectHandler : Connectable<Any, MobiusCounterEvent> {
  override fun connect(output: Consumer<MobiusCounterEvent>) =
      object : Connection<Any> {
        override fun accept(value: Any) = Unit
        override fun dispose() = Unit
      }
}

fun main(args: Array<String>) {
  val loop = Mobius.loop(MobiusUpdateFunc(), EffectHandler())
      .startFrom(CounterModel())

  loop.observe(::println)

  arrayOf(
      MobiusCounterEvent.Inc,
      MobiusCounterEvent.Dec,
      MobiusCounterEvent.Add(10),
      MobiusCounterEvent.Sub(10)
  ).forEach(loop::dispatchEvent)
}
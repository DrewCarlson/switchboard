package io.hypno.switchboard


data class CounterState(val count: Int = 0) {
  operator fun plus(number: Int) = copy(count = count + number)
  operator fun minus(number: Int) = copy(count = count - number)
}

@Switchboard(
    patchFunParams = [CounterState::class],
    patchFunParamNames = ["state"],
    connectionBaseClass = CounterEvent::class,
    connectionReturnClass = CounterState::class
)
sealed class CounterEvent {
  object Inc : CounterEvent()
  object Dec : CounterEvent()
  data class Add(val number: Int) : CounterEvent()
  data class Sub(val number: Int) : CounterEvent()
}

class CounterImpl : CounterEventSwitchboard {
  override fun patch(state: CounterState, connection: CounterEvent) =
      super.patch(state, connection).apply { println(this) }

  override fun inc(state: CounterState) = state + 1
  override fun dec(state: CounterState) = state - 1
  override fun add(state: CounterState, connection: CounterEvent.Add) = state + connection.number
  override fun sub(state: CounterState, connection: CounterEvent.Sub) = state - connection.number
}

fun main(args: Array<String>) {
  CounterImpl().apply {
    patch(CounterState(0), CounterEvent.Inc)
    patch(CounterState(1), CounterEvent.Dec)
    patch(CounterState(0), CounterEvent.Add(10))
    patch(CounterState(10), CounterEvent.Sub(10))
  }
}
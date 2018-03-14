package io.hypno.switchboard


data class CounterState(val count: Int = 0)

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
  override fun add(state: CounterState, connection: CounterEvent.Add) =
      state.copy(count = state.count + connection.number)

  override fun sub(state: CounterState, connection: CounterEvent.Sub) =
      state.copy(count = state.count - connection.number)

  override fun inc(state: CounterState) = state.copy(count = state.count + 1)
  override fun dec(state: CounterState) = state.copy(count = state.count - 1)
}

fun main(args: Array<String>) {
  val counter = CounterImpl()

  var state = CounterState()
  println("initial state: $state")
  state = counter.patch(state, CounterEvent.Inc)
  println("state($state) should equal 1")
  state = counter.patch(state, CounterEvent.Dec)
  println("state($state) should equal 0")
  state = counter.patch(state, CounterEvent.Add(10))
  println("state($state) should equal 10")
  state = counter.patch(state, CounterEvent.Sub(10))
  println("state($state) should equal 0")
}
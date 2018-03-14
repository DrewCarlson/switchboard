package io.hypno.switchboard

import com.spotify.mobius.Next
import com.spotify.mobius.Update

data class CounterModel(val count: Int)

@Switchboard(
    patchFunParams = [CounterModel::class],
    patchFunParamNames = ["model"],
    connectionBaseClass = MobiusCounterEvent::class,
    connectionParamName = "event",
    connectionReturnClass = Next::class,
    connectionReturnProjections = [(CounterModel::class), (Unit::class)]
)
sealed class MobiusCounterEvent {
  object onInc : MobiusCounterEvent()
  object onDec : MobiusCounterEvent()
  data class onAdd(val number: Int) : MobiusCounterEvent()
  data class onSub(val number: Int) : MobiusCounterEvent()
}

class MobiusUpdateFunc : Update<CounterModel, MobiusCounterEvent, Unit>,
    MobiusCounterEventSwitchboard {
  override fun update(model: CounterModel, event: MobiusCounterEvent) = patch(model, event)

  override fun onInc(model: CounterModel): Next<CounterModel, Unit> =
      Next.next(model.copy(count = model.count+1))

  override fun onDec(model: CounterModel): Next<CounterModel, Unit> =
      Next.next(model.copy(count = model.count-1))

  override fun onAdd(model: CounterModel, event: MobiusCounterEvent.onAdd): Next<CounterModel, Unit> =
      Next.next(model.copy(count = model.count + event.number))

  override fun onSub(model: CounterModel, event: MobiusCounterEvent.onSub): Next<CounterModel, Unit> =
      Next.next(model.copy(count = model.count - event.number))
}
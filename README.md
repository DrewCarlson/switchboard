[![Release](https://jitpack.io/v/DrewCarlson/switchboard.svg)](https://jitpack.io/#DrewCarlson/switchboard)

# switchboard
Generate Kotlin state mutation specs from sealed classes.

## Example
This is a basic example that functions without any libraries.

```kotlin
// A simple State model that holds a number.
data class CounterState(val count: Int)

@Switchboard(
    patchFunParams = [CounterState::class],
    patchFunParamNames = ["state"],
    connectionBaseClass = CounterEvent::class,
    connectionReturnClass = CounterState::class)
sealed class CounterEvent {
  object Inc : CounterEvent()
  object Dec : CounterEvent()
  data class Add(val number: Int) : CounterEvent()
  data class Sub(val number: Int) : CounterEvent()
}
```
The following switchboard will be generated.
```kotlin
interface CounterEventSwitchboard {
    fun patch(state: CounterState, connection: CounterEvent): CounterState = when (connection) {
        CounterEvent.Inc -> inc(state)
        CounterEvent.Dec -> dec(state)
        is CounterEvent.Add -> add(state, connection)
        is CounterEvent.Sub -> sub(state, connection)
    }

    fun inc(state: CounterState): CounterState
    fun dec(state: CounterState): CounterState
    fun add(state: CounterState, connection: CounterEvent.Add): CounterState
    fun sub(state: CounterState, connection: CounterEvent.Sub): CounterState
}
```

## Download
Switchboard is available via Jitpack.
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency:
```
dependencies {
  implementation 'com.github.DrewCarlson.switchboard:api:VERSION'
  kapt 'com.github.DrewCarlson.switchboard:generator:VERSION'
}
```

## Framework example
Here is an example that generates switchboards suitable for [Mobius](https://github.com/spotify/mobius) `Update<M, E, F>` functions.
In this example, we don't need any Effects, so we'll just use Unit but any non-primative type will work.
```kotlin
import com.spotify.mobius.Next
import com.spotify.mobius.Update

// A simple State model that holds a number.
data class CounterModel(val count: Int)

@Switchboard(
    patchFunParams = [CounterModel::class],
    patchFunParamNames = ["model"],
    connectionBaseClass = MobiusCounterEvent::class,
    connectionParamName = "event",
    connectionReturnClass = Next::class,
    connectionReturnProjections = [CounterModel::class, Unit::class]
)
sealed class MobiusCounterEvent {
  object onInc : MobiusCounterEvent()
  object onDec : MobiusCounterEvent()
  data class onAdd(val number: Int) : MobiusCounterEvent()
  data class onSub(val number: Int) : MobiusCounterEvent()
}
```
The following switchboard will be generated.
```kotlin
import com.spotify.mobius.Next

interface MobiusCounterEventSwitchboard {
    fun patch(model: CounterModel, event: MobiusCounterEvent): Next<CounterModel, Unit> = when (event) {
        MobiusCounterEvent.onInc -> onInc(model)
        MobiusCounterEvent.onDec -> onDec(model)
        is MobiusCounterEvent.onAdd -> onAdd(model, event)
        is MobiusCounterEvent.onSub -> onSub(model, event)
    }

    fun onInc(model: CounterModel): Next<CounterModel, Unit>
    fun onDec(model: CounterModel): Next<CounterModel, Unit>
    fun onAdd(model: CounterModel, event: MobiusCounterEvent.onAdd): Next<CounterModel, Unit>
    fun onSub(model: CounterModel, event: MobiusCounterEvent.onSub): Next<CounterModel, Unit>
}
```
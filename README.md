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

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
```groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency:
```groovy
dependencies {
  implementation 'com.github.DrewCarlson.switchboard:api:VERSION'
  kapt 'com.github.DrewCarlson.switchboard:generator:VERSION'
  
  // Or with Mobius
  implementation 'com.github.DrewCarlson.switchboard:api-mobius:VERSION'
  kapt 'com.github.DrewCarlson.switchboard:generator-mobius:VERSION'
}
```

## Mobius Spec Generator
The `generator` module doubles as the `@Switchboard` generator and an extension point for providing specialized Switchboard generators.

This project provides `api-mobius` and `generator-mobius` as 
1) an example of how to generate custom Switchboards and
2) a practical library for generating [Mobius](https://github.com/spotify/mobius) `Update<M, E, F>` and `Connectable<F, E>`(effect handler) function specs.

Example:
```kotlin
@MobiusUpdateSpec(
    modelName = "Login",
    baseModel = LoginState::class,
    baseEffect = BaseEffect::class)
sealed class LoginEvent : BaseEvent {
  object OnSubmitLoginClicked : LoginEvent()
  object OnForgotPasswordClicked : LoginEvent()
  object OnLoginSuccess : LoginEvent()

  data class OnEmailChanged(val email: String) : LoginEvent()
  data class OnPasswordChanged(val password: String) : LoginEvent()
  data class OnLoginRequestError(val error: String) : LoginEvent()
  data class OnLoginValidationError(val emailError: String, val passwordError: String) : LoginEvent()
}
```
The following update spec will be generated.
```kotlin
interface LoginUpdateSpec {
    fun patch(model: LoginState, event: BaseEvent): Next<LoginState, Any> = when (event) {
        LoginEvent.OnSubmitLoginClicked -> onSubmitLoginClicked(model)
        LoginEvent.OnForgotPasswordClicked -> onForgotPasswordClicked(model)
        LoginEvent.OnLoginSuccess -> onLoginSuccess(model)
        is LoginEvent.OnEmailChanged -> onEmailChanged(model, event)
        is LoginEvent.OnPasswordChanged -> onPasswordChanged(model, event)
        is LoginEvent.OnLoginValidationError -> onLoginValidationError(model, event)
        is LoginEvent.OnLoginRequestError -> onLoginRequestError(model, event)
        else -> drop(model, event)
    }

    fun drop(model: LoginState, event: AppEvent): Next<LoginState, Any>
    fun onSubmitLoginClicked(model: LoginState): Next<LoginState, Any>
    fun onForgotPasswordClicked(model: LoginState): Next<LoginState, Any>
    fun onLoginSuccess(model: LoginState): Next<LoginState, Any>
    fun onEmailChanged(model: LoginState, event: LoginEvent.OnEmailChanged): Next<LoginState, Any>
    fun onPasswordChanged(model: LoginState, event: LoginEvent.OnPasswordChanged): Next<LoginState, Any>
    fun onLoginRequestError(model: LoginState, event: LoginEvent.OnLoginRequestError): Next<LoginState, Any>
    fun onLoginValidationError(model: LoginState, event: LoginEvent.OnLoginValidationError): Next<LoginState, Any>
}
```

## Framework example
*Note: This section remains as an example advanced `@Switchboard` usage, if you intend to use Switchboard with Mobius use the provided [Mobius Generator]().*

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


## License

    Copyright 2018 Andrew Carlson

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.'

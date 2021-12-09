[![Version](https://img.shields.io/maven-central/v/dev.hichamboushaba.suspendactivityresult/suspendactivityresult)](https://repo1.maven.org/maven2/dev/hichamboushaba/suspendactivityresult/)

## SuspendActivityResult

A lightweight library for requesting and
consuming [Activity Results](https://developer.android.com/reference/androidx/activity/result/ActivityResultCaller#registerForActivityResult(androidx.activity.result.contract.ActivityResultContract%3CI,O%3E,androidx.activity.result.ActivityResultCallback%3CO%3E))
using coroutines, it's usage is as simple as:

```kotlin
val uri = ActivityResultManager.getInstance().requestResult(
    contract = GetContent(),
    input = "image/*"
)
```

or using the built-in extensions:

```kotlin
val uri = ActivityResultManager.getInstance().getContent("image/*")
```

For more information, check the
articles: [Part 1](https://dev.to/hichamboushaba/consuming-activity-results-using-coroutines-part-1-2j57)
and [Part 2](https://dev.to/hichamboushaba/consuming-activity-results-using-coroutines-part-2-54mf)

### Runtime Permissions

The library offers a utility class for requesting runtime permissions with access to
the [shouldShowRequestPermissionRationale](https://developer.android.com/reference/android/app/Activity#shouldShowRequestPermissionRationale(java.lang.String))'s
value:

```kotlin
val result = PermissionManager.getInstance()
    .requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
when (result) {
    PermissionGranted -> {
        // TODO
    }
    is PermissionDenied -> {
        if (result.shouldShowRationale) {
            // TODO
        } else {
            // TODO
        }
    }
}
```

This class uses internally `ActivityResultManager.getInstance().requestPermission()`, so everything
below applies to it as well.

### Download

```groovy
implementation 'dev.hichamboushaba.suspendactivityresult:suspendactivityresult:0.1.3'
```

The default artifact uses [App Startup](https://developer.android.com/topic/libraries/app-startup)
for the initialization.

If you don't want this dependency added, you can use the other variant:

```groovy
implementation 'dev.hichamboushaba.suspendactivityresult:suspendactivityresult-no-startup:0.1.3'
```

And initialize the library manually

```kotlin
class App : Application() {
    fun onCreate() {
        super.onCreate()
        ActivityResultManager.init(this)
    }
}
```

### Testing

`ActivityResultManager` is an interface, so for better testability, it's recommended to
inject `ActivityResultManager.getInstance()`
into your components, for easier swapping to a fake or mocked implementation for tests.

### Process-death

The implementation of `requestResult` takes into account process-death scenarios, and keeps track of
the pending operation using the
Activity's [SavedStateRegistry](https://developer.android.com/reference/androidx/savedstate/SavedStateRegistry)
, which means calling `requestResult` after a process-death, will not re-launch the activity result
caller, and instead, it will only register the callback to allow receiving the result. And to make
this work as expected, the application need to keep the screen's state across this process-death, to
call `requestResult` afterwards, check
the [example app](./app/src/main/java/com/hicham/activityresult/files/ExternalFilesViewModel.kt) for
how we can implement this
using [SavedStateHandle](https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle)
.
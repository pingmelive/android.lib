# Library to handle error

This library allows you to get all the error from your android app to pindMeLive application.

## How to use

### One-step install

Add the following dependency to your build.gradle:
```gradle
dependencies {
   implementation 'com.github.pingmelive:pingMeLive:1.0.2'
}
```

### Installation

Add a snippet like this to your `Application` class:

```java
@Override
public void onCreate() {
    super.onCreate();

      pingMeLive.install(getApplicationContext());
      //thats it 
      
}
```

...and you are done!

Of course, you can combine this library with any other crash handler such as Crashlytics, ACRA or Firebase, just set them up as you would normally.

### Try it

Force an app crash by throwing an uncaught exception, using something like this in your code:
```java
throw new RuntimeException("A dummy error!! No more force stop dialogs!");
```



### Advanced setup

You can customize the behavior of this library in several ways by setting its configuration at any moment.
However, it's recommended to do it on your `Application` class so it becomes available as soon as possible.

Add a snippet like this to your `Application` class:
```java
@Override
public void onCreate() {
    super.onCreate();

    pingMeLive.Builder.create()
        .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: pingMeLive.BACKGROUND_MODE_SHOW_CUSTOM
        .enabled(false) //default: true
        .showErrorDetails(false) //default: true
        .showRestartButton(false) //default: true
        .logErrorOnRestart(false) //default: true
        .trackActivities(true) //default: false
        .minTimeBetweenCrashesMs(2000) //default: 3000
        .errorDrawable(R.drawable.ic_custom_drawable) //default: bug image
        .restartActivity(YourCustomActivity.class) //default: null (your app's launch activity)
        .errorActivity(YourCustomErrorActivity.class) //default: null (default error activity)
        .eventListener(new YourCustomEventListener()) //default: null
        .apply();
}
```

## Using Proguard?

No need to add special rules, the library should work even with obfuscation.

## Inner workings

This library relies on the `Thread.setDefaultUncaughtExceptionHandler` method.
When an exception is caught by the library's `UncaughtExceptionHandler` it does the following:

1. Captures the stack trace that caused the crash
2. Launches a new intent to the error activity in a new process passing the crash info as an extra.
3. Kills the current process.

## Disclaimers

* This will not avoid ANRs from happening.
* This will not catch native errors.
* There is no guarantee that this will work on every device.
* This library will not make you toast for breakfast :)

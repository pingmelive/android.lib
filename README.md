# Library to handle error

This library allows you to get all the error from your android app to pindMeLive application.

## How to use

### Register yourself on
[https://pingmelive.com](https://pingmelive.com)
You will get your API KEY after registeration.

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

      //By default you will get all the crashes and runtime error in a form of error event.
      //You will get an API KEY when you will register on pingmelive.com

      String appName = "MyGreatApp";
      String errorGroupTitle = "Error for "+appName;
      
      pingMeLive.install(getApplicationContext(),errorGroupTitle,"API_KEY");
      
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

## Custom Events

You can also use pingMeLive for sending custom events.

### 1.Simple event

```java

String userID = "userabc";

Button registerUser = findViewById(R.id.registerUser);
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               //Pass group title and your custom event message you want to send.
                pingMeLive.simpleEvent("Registeration","Hey we got a new user "+userID);

            }
        });
        

```


If you want to send some detailed long description you can use `Detailed event`
### 2.Detailed event

```java

String userID = "userabc";

Button registerUser = findViewById(R.id.registerUser);
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               //Pass group title and your custom event message you want to send and detailed text
                pingMeLive.detailedEvent("Registeration","Hey we got a new user "+userID,"You can send the user detail here.");

            }
        });
        

```

## Some usefull information

* Only `Detailed event` will by default contain the information like device info,app version code etc.
* If you only want error event just install the library and thats it, no need to code anything else.
* You can smartly use group title for you custom events.

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

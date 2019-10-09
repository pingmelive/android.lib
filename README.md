# What is PingMeLive?

This is a library which helps you to get LIVE notifications of actions taking place on your webistes and applications.
Just Copy past the below codes and get live updates of errors and actions. Make Categories of pings based on projects (etc) and assign it to your team mates.
That's what PingMeLive does.
Easy right!.

## How to use

### Register yourself on

[https://pingmelive.com](https://pingmelive.com) **and get your `API KEY`**.

### One-step install

Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
	repositories {
		maven { url 'https://www.jitpack.io' }
	}
}
```


Add the following dependency to your build.gradle:
```gradle
dependencies {
      implementation 'com.github.pingmelive:android.lib:1.0.14'
}
```

### Installation

Add a snippet like this to your `Application` class:

```java
@Override
public void onCreate() {
    super.onCreate();


         new pingMeLive.Builder(getApplicationContext())
                .setErrorEventEnabled(true) //By Default True - This will send error events to you
                .setErrorEventTitle("ERROR_TITLE") //Error Event title
                .setAPI_KEY("YOUR_API_KEY") //Your API KEY
                .setPROJECT_ID("YOUR_PROJECT_ID") //Your Project ID
                .install();
                      
      //By default you will get all the crashes and runtime error in a form of error event.
      //You will get an API KEY when you will register on pingmelive.com 
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

               //Pass `groupTitle` and your custom `eventMessage`.
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

               //Pass `groupTitle` and your custom `eventMessage` you want to send and `detailDescription`
                pingMeLive.detailedEvent("Registeration","Hey we got a new user "+userID,"You can send the user detail here.");

            }
        });
        

```


### Options
* **apiKey** : To get an `API KEY` , register on pingmelive.com. Its free to use.
* **projectID** : Once registered, Click on New Project to create. 
* **errorStatus** : `true` / `false` (Boolen Value).
* **errorName** : This will be your `Group Title/Name` where all the error will be pinged.(This works when `errorStatus` is set as `true`.
* **groupTitle** : This will be your `Group Tilte/Name` under which , you will get all your pings.
* **eventMessage** : The `errorMessage` is limited to 360 character in length. Any additional characters beyond 360 character will be truncated.
* **detailDescription** : The `detailDescription` is does not have any length limitation. You can also send JSON Formatted String / or simple plain string.


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
* This library will not make you toast for breakfast :)

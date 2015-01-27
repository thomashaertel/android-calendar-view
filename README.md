# Android Calendar View
[![Download](https://api.bintray.com/packages/thomashaertel/maven/android-calendar-view/images/download.svg) ](https://bintray.com/thomashaertel/maven/android-calendar-view/_latestVersion)
[![Build Status](https://travis-ci.org/thomashaertel/android-calendar-view.svg?branch=master)](https://travis-ci.org/thomashaertel/android-calendar-view)

Android Calender View is fork from [https://code.google.com/p/android-calendar-view/](https://code.google.com/p/android-calendar-view/) to support using it from Android Studio as a library.

## Overview
Android does not offer any calendar view in the SDK. This project is an option for that. Developer can import and use CalendarView to display a specified date in a month view, or let user pick up a date from it.  

<img src="http://farm6.static.flickr.com/5053/5415600440_f1c486c2d4.jpg" alt="Spinner with single selection" width="5%" height="50%">

## Usage
Add the following code to an activity or fragment to use the calendar view:
 ```java
    private OnDateSetListener listener = new OnDateSetListener() {
        @Override
        public void onDateSet(CalendarView view, int year, int monthOfYear, int dayOfMonth) {
            // Place your code here
        }
    };
     
    public CalendarPickerDialog createCalendarPickerDialog(Calendar calendar) {
        int cyear = calendar.get(Calendar.YEAR);
        int cmonth = calendar.get(Calendar.MONTH);
        int cday = calendar.get(Calendar.DAY_OF_MONTH);
        
        CalendarPickerDialog calendarPicker = new CalendarPickerDialog(getActivity(), listener, cyear, cmonth, cday);
        return calendarPicker;
    }
```

## Building
### Gradle

#### From Bintray

Add maven central to your `build.gradle`:

```groovy
buildscript {
  repositories {
    jcenter()
  }
}

repositories {
  jcenter()
}
```

#### From maven central

Add maven central to your `build.gradle`:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
}

repositories {
  mavenCentral()
}
```

Then declare Android Calendar View within your dependencies:

```groovy
dependencies {
  ...
  compile('com.thomashaertel:android-calendar-view:0.5.0@aar') {
  }
  ...
}
```

### Maven

#### From maven central

To use Android Calendar View within your maven build simply add

```xml
<dependency>
  <artifactId>android-calendar-view</artifactId>
  <version>${android-calendar-view.version}</version>
  <groupId>com.thomashaertel</groupId>
</dependency>
```

to your pom.xml

If you also want the sources or javadoc add the respective classifier

```xml
  <classifier>sources</classifier>
```

or

```xml
  <classifier>javadoc</classifier>
```
to the dependency.

## License

* [Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
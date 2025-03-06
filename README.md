# PerfectPace Timer

PerfectPace Timer is an Android application designed to provide structure and discipline for personal weight-loss through a dynamic, Pomodoro-inspired timer. The app empowers you to set flexible work/rest ratios based on real-time input, supporting incremental progress in your daily routine.

## Table of Contents
- [Features](#features)
- [Architecture & Key Components](#architecture--key-components)
- [Technical Specifications](#technical-specifications)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Flexible Work Sessions**  
  Work sessions act as a stopwatch with a duration defined by the user. When you initiate a session, the `TimerService` starts the stopwatch immediately, providing a custom-timed work period.

- **Dynamic Break Mode**  
  When you decide to take a break, the app seamlessly switches modes. The stopwatch converts into a countdown timer that starts from the elapsed time of your preceding work session. This switch is managed by setting `isBreakMode` to true and calculating the appropriate `breakDuration` in the `TimerService`.

- **Audio Alerts for Enhanced Time Awareness**  
  Every 30 minutes during a work session, audio alerts remind you to stay mindful of your progress. The `timerRunnable` in `TimerService` checks for multiples of 1800 seconds (30 minutes) and triggers an alert sound.

- **Persistent Background Timer**  
  The core timer continues to run in the background using a persistent foreground service (`TimerService`). This service, declared with a `foregroundServiceType` of `specialUse` in the manifest, ensures continuous operation even when the app isn’t front and center.

- **Modern UI with Jetpack Compose**  
  Leveraging Jetpack Compose, the app presents a clean and responsive UI on a dedicated `TimerScreen`, giving you real-time visual feedback of the current session.

- **Real-Time UI Updates using BroadcastReceiver**  
  The `TimerBroadcastReceiver` listens for time updates sent from the `TimerService` and updates the `currentTime` state in `MainActivity` through a listener interface. This guarantees that your timer display remains accurate and in sync with the service.

- **Direct Service Binding from MainActivity**  
  MainActivity binds to the `TimerService` using a ServiceConnection. This connection empowers you to control timer functionalities directly from the UI, such as starting the stopwatch (`startStopwatch`) or switching to break mode (`switchToBreakMode`).

## Architecture & Key Components

- **TimerService**  
  A foreground service that handles starting, maintaining, and transitioning between work sessions and break periods. It also manages the periodic audio alerts and ensures the timer runs reliably in the background.

- **TimerBroadcastReceiver**  
  This component receives `TIMER_UPDATE` broadcasts from the `TimerService` and refreshes the UI in MainActivity, keeping the displayed time accurate.

- **MainActivity**  
  The primary interface built with Jetpack Compose. It binds to the TimerService to initiate and control timer functions and listens for updates through the TimerBroadcastReceiver.

## Technical Specifications

- **Programming Language**: Kotlin  
- **Target SDK**: Android SDK 35  
- **Minimum SDK**: Android SDK 21  
- **UI Framework**: Jetpack Compose  
- **Build Tool**: Android Gradle Plugin  

## Usage

1. **Start a Work Session**  
   Launch the app and initiate a work session. The stopwatch in `TimerService` will begin timing your flexible work session immediately.

2. **Switch to Break Mode**  
   During a work session, trigger the mode switch to transition into break mode. The countdown will start based on your elapsed work session time, providing a natural break period.

3. **Receive Audio Alerts**  
   Keep track of your progress with audio alerts every 30 minutes, ensuring you remain aware of the passing time during long work sessions.

4. **Background Operation**  
   The persistent foreground service guarantees that your timing continues unabated, even if the app is not visible.

## Contributing

PerfectPace Timer is a personal project streamlined for learning Android fundamentals, especially in background services and timer implementations. Contributions that improve reliability, functionality, or the user experience are welcome. Feel free to fork the repository and submit pull requests or suggestions.

## License

This project is provided for personal use and continued learning. Please refer to the repository's license file for more details.

---

PerfectPace Timer is a journey into the heart of Android development, balancing practicality with the excitement of learning new techniques in background processing and UI design. Enjoy crafting your work/rest routine, and let this project inspire further exploration into the possibilities of Android app development!

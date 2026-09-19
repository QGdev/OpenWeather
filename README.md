![OpenWeather — free weather, no account, no tracking](art/feature-graphic.jpg)

# OpenWeather

Android weather application based on OpenWeatherMaps APIs.

[![Licence](https://img.shields.io/badge/licence-GPLv3-blue)](LICENSE)
![Platform](https://img.shields.io/badge/platform-Android%2011%2B-3DDC84)
![Version](https://img.shields.io/badge/version-0.10.0-orange)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)

OpenWeather is an open-source weather application that clearly and simply displays the weather for a
multitude of cities. The app is under the GPLv3 licence, please respect the terms of this licence.

The application is committed to not collecting any user data, only city data is sent to
OpenWeatherMaps.

Since 0.10.0 the whole interface has been rewritten in Jetpack Compose with Material 3, every city
now taking the colour of its own sky and opening on a screen of its own.

## ✨ Features

* **Every city under its own sky.** Cards take the colour of the weather: clear, cloudy, rain,
  thunderstorm, snow, by day and by night.
* **A screen for each place**, with its current conditions and all its measurements.
* **Rain minute by minute** for the next hour, shown only when there is something to show.
* **Hourly forecasts** for 48 hours and **daily forecasts** for 8 days, as readable graphs.
* **Air quality**, with its six pollutants and an explanation of the index.
* **Official weather alerts**, in the words of the agency that issues them.
* Feels-like temperature, wind and gusts, humidity, pressure, visibility, dew point, UV index,
  sunrise and sunset, moonrise and moonset.
* **Home screen widgets** in three sizes, with the transparency of your choice.
* **Your units, your language:** temperature, pressure, wind, distance and time formats are yours to
  choose, and the language of the application can be set per app in the Android settings.

## 🔒 Privacy

Privacy is a fundamental right, that's why we only take the bare minimum and we prove it by making
the application open-source.

Only the necessary data are used by the application, these data are:
* OpenWeatherMaps API key, only used to retrieve data from registered cities by the user.
* Registered cities, only used to know for which city the application need to search and display data.

The API key is stored encrypted on the device and the registered cities never leave it, only the
weather requests do. City searching goes through OpenStreetMap Nominatim.

OpenWeatherMaps privacy policy:
[openweather.co.uk/privacy-policy](https://openweather.co.uk/privacy-policy)

## 📲 Availability on apps markets

### Google Play Store

For now it is only available on the PlayStore as a closed alpha, there is the possibility to add
people on request. But I will release it on public soon on the PlayStore.

### F-Droid

For now it is not available on F-Droid but I am thinking to put my app on it.

## 🔑 Getting started

The application needs your own OpenWeatherMap API key, a free one being enough: create it on
[openweathermap.org](https://openweathermap.org/api) and the application will ask for it on its
first run. A free key allows one call per minute, which is why the update period cannot be set below
five minutes.

Android 11 or newer is required.

## 🛠️ Building

```bash
./gradlew assembleDebug        # Debug APK
./gradlew assembleRelease      # Release APK
./gradlew test                 # Unit tests
```

## 📈 Version history

### 0.7 — the first releases

* **0.7.6**, February 2021
  * places, current weather, hourly and daily forecasts
  * a splash screen
  * the place's time or yours, in 12 or 24 hours
* **0.7.7**, February 2021
  * French translation
  * weather descriptions asked in the language of the device

### 0.8 — alerts and a faster list

* **0.8**, February 2021 — weather alerts
* **0.8.2**, March 2021 — alerts fixed on large displays
* **0.8.6**, May 2021
  * place cards moved to a RecyclerView
  * vector drawables in place of PNG, cutting the application size by 15%
* **0.8.8**, August 2021
  * pull to refresh reworked
  * extended language support
  * an about section with its attributions

### 0.9 — units, air quality and an architecture

* **0.9.0**, October 2021
  * unit settings through FormattingService
  * forecasts drawn by custom graph views
* **0.9.2**, June 2022
  * air quality
  * the GPLv3 licence and documented sources
  * weather data stored encrypted
* **0.9.3**, April 2023
  * Room database, with a repository and ViewModel architecture
  * Material You colours
* **0.9.4**, July 2023
  * places reorderable by hand
  * the first unit tests and a pass on code quality

### 0.10 — the application rebuilt

* **0.10.0**, August 2025 – September 2026

  *Written on the spare evenings of an office job, as motivation came.*

  * the whole interface redrawn and rewritten in Jetpack Compose with Material 3, leaving fragments, XML layouts and custom views behind
  * the codebase moved from Java to Kotlin
  * storage moved from a Room database to a protobuf DataStore
  * a screen of its own for each place
  * home screen widgets reworked
  * a first run onboarding
  * a language chosen per application

## 🤝 Contributing

Issues and merge requests are welcome. Clone the repository, open it in Android Studio, and
`./gradlew assembleDebug` is all it takes to get started.

A few things that make a merge request easier to accept:

* new code written in Kotlin, the migration away from Java being almost over
* the GPLv3 header on every new file
* `./gradlew test` passing
* one subject per merge request

## ⚠️ Disclaimer

This application is powered by the OpenWeatherMaps APIs but has no connection or affiliation with
this company.

## ⚖️ Credits

### Weather data and Forecasts

Weather data and forecasts are acquired through [openweathermap.org](https://openweathermap.org/),
they are licensed under ODbL.
### API OpenWeatherMaps

Use of [openweathermap.org](https://openweathermap.org/) APIs is licensed under CC BY-SA 4.O.
### Place searching

Place searching is done through [OpenStreetMap Nominatim](https://nominatim.openstreetmap.org/), its
data being licensed under ODbL.
### Icons

Icons made by [freepik.com](https://freepik.com) from [flaticon.com](https://flaticon.com).
### Typefaces

[Figtree](https://fonts.google.com/specimen/Figtree) and [IBM Plex
Mono](https://fonts.google.com/specimen/IBM+Plex+Mono), both under the SIL Open Font License.

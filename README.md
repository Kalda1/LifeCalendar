\# Memento Mori - Live Wallpaper Generator (Android)



A minimal, "Nothing OS" inspired live wallpaper generator that visualizes your life progress on your lock screen. It uses a dot-matrix grid to represent weeks lived versus weeks remaining based on statistical data.



\## Features

\- \*\*Nothing OS Aesthetics:\*\* Clean, monochrome, dot-matrix design.

\- \*\*Dynamic Grid:\*\* Calculates weeks lived based on your birth date.

\- \*\*Smart Crop:\*\* Supports custom background images from your gallery with intelligent center-cropping.

\- \*\*Auto-Update:\*\* Uses Android WorkManager to update the grid automatically every week (adding a new dot).

\- \*\*Privacy Focused:\*\* All data and images remain locally on your device. No internet required.



\## How it works

1\. \*\*Set Birth Date:\*\* The app calculates your life progress (based on a 76-year average).

2\. \*\*Choose Background:\*\* Use the default "Nothing" leaf wallpaper or pick your own.

3\. \*\*Generate:\*\* The app draws a custom bitmap and applies it to your Lock Screen.



\## Tech Stack

\- \*\*Language:\*\* Kotlin

\- \*\*UI:\*\* XML Layouts (ConstraintLayout)

\- \*\*Background Tasks:\*\* Android WorkManager

\- \*\*Graphics:\*\* Canvas API for drawing the grid and processing images.



\## Setup

1\. Clone the repository.

2\. Open in Android Studio.

3\. Build \& Run on your device.



---

\*Concept inspired by the Stoic "Memento Mori" calendar and Tim Urban's "Your Life in Weeks".\*


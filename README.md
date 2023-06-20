# speech-recognition
An android app for recognizing code words and numbers
# Speech Command Recognition

Speech Command Recognition is an Android application that uses the SpeechRecognizer API to recognize spoken commands and perform corresponding actions.
It allows users to give voice commands such as "code," "count," "back," and "reset," and handles speech-to-text conversion and command recognition.

## Features

- Recognizes spoken commands and performs actions based on the command.
- Supports both English (US) language and German (DE) with synonyms handling for improved recognition accurac.
- Displays recognized commands in a command list.
- Provides visual feedback for live speech recognition.


## Limitations

- **Number Recognition:** 
- The current implementation of the speech recognizer faces challenges in reliably distinguishing between "12" (twelve) and "1 2" (one two), resulting in inconsistent recognition results. Additionally, the recognizer struggles to exclude numbers over 10. To address this limitation, one potential solution is to leverage Google Cloud's Model Adaptation, which allows for prioritizing certain words and improving recognition accuracy. However, the current implementation relies on Android's built-in speech recognizer, which lacks such capabilities and operates solely offline.
- **Live Results:** 
- During the speech recognition process, the onPartialResults method is called multiple times, which can lead to difficulties in processing live results without encountering redundant data. To ensure accurate and non-repetitive command processing, the application processes the full final results only after the onResults method is triggered when the recording stops.
- **Background Hotword Trigger:** 
- The speech recognizer is actively engaged in recording, which means it is not suitable for a background hotword trigger functionality. While it is possible to implement a hotword trigger using the VoiceInteractionService, acquiring the required level of permission is typically limited to system apps. Alternatively, there might be third-party libraries available that specialize in hotword triggers. As the current implementation lacks a hotword trigger, the speech recording is manually initiated by tapping the microphone icon.

## Requirements

- Android device running Android 5.0 (Lollipop) or later.
- Microphone permission granted.

## Installation

1. Clone the repository:
2. Open the project in Android Studio.
3. Build and run the application on your Android device or emulator.

## Usage

1. Launch the Speech Command Recognition application on your Android device.
2. Grant the microphone permission when prompted.
3. Tap the microphone icon to start speech recognition.
4. Speak one of the supported commands: "code [number]", "count [number]", "back", or "reset".
6. The application will recognize the command and perform the corresponding action.
7. View the recognized commands in the command list.

## Customization

You can customize the application by modifying the following:

- Add additional commands by extending the switch statement in the `onResults` method of the `MainActivity` class.
- Modify the handling of synonyms in the `handleSynonyms` method of the `MainActivity` class to support different languages or synonyms.
- Customize the UI by modifying the layout XML files in the `res/layout` directory.


## Limitation

QuizApp

To get started with our app, clone the repository from GitHub using this link: https://github.com/ST10082831/OPSCQUIZAPP.git
Purpose of the App

The QuizApp is a mobile application that allows users to engage in quizzes across various categories and difficulty levels. Users can start new quizzes, continue previous games, view their performance on a leaderboard, customize settings like language preferences, dark mode, and sound options, and enable biometric login for added security. The app uses Firebase for authentication and Firestore for storing game data, including saved games and leaderboard scores.
Key Features

    Authentication: Users can register, log in, manage profiles, and enable biometric login for secure, quick access.
    Biometric Login: Users have the option to enable fingerprint or face authentication for a seamless login experience.
    Google Sign-In: Quick login using Google accounts.
    Multilingual Support: The app supports multiple languages, including English, Afrikaans, and Zulu, allowing users to switch between languages easily.
    Quiz Categories: Users can select from various quiz categories and difficulty levels before starting a quiz.
    Continue Game Functionality: Users can continue a quiz from where they left off, with reminders sent via notifications if a saved game exists.
    Leaderboard: Track quiz performance and compare scores with others in real-time.
    Dark Mode and Sound Settings: Users can toggle between light and dark modes and manage sound preferences.
    Notification System: The app notifies users when a saved game is available to continue.

Design / Screens

    Main Menu: Options to start a new game, continue a previous game, view the leaderboard, and access settings.
    Quiz Interface: A clear and user-friendly layout displaying questions and answers with a 'Next' button for easy navigation.
    Settings: Users can modify language preferences, enable biometric login, toggle dark mode, adjust sound settings, and log out.

Firebase Integration

The app integrates Firebase for the following:

    Authentication: User accounts are managed using Firebase Authentication, including Google Sign-In.
    Firestore Database: Used for game state management and leaderboard storage, allowing users to save their progress and view real-time results.
    Push Notifications: Firebase Cloud Messaging is utilized for push notifications to alert users of any saved games they can continue.

Quiz API

The app fetches quiz questions using the Open Trivia Database (OpenTDB) API. Questions are categorized based on the user’s selected preferences, including category and difficulty. The API provides both correct and incorrect answers, allowing users to engage with dynamic quiz content.
Version Control and GitHub Usage

The project is version-controlled using Git and hosted on GitHub. This allows for efficient tracking of changes made to project files and enables collaboration among developers. GitHub Issues and Pull Requests are used for tracking bugs, enhancements, and collaborative contributions to the project.

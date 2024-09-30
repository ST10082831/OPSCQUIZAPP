QuizApp

To use our app please clone it using Github with this link: https://github.com/ST10082831/OPSCQUIZAPP.git
Purpose of the App

The QuizApp is a mobile application that allows users to engage with quizzes across various categories and difficulties. 
Users can start new quizzes, continue previous games, view their performance on a leaderboard, and customize settings such as language preferences, dark mode, and sound settings. 
The app uses Firebase for authentication and Firestore for storing game data, including saved games and leaderboard scores.

Key Features:
Authentication: Users can register, log in, and manage their profiles.
Multilingual Support: The app supports multiple languages, allowing users to switch between languages.
Quiz Categories: Users can select quiz categories and difficulty levels before starting a quiz.
Continue Game Functionality: Users can continue their quiz from where they left off.
Leaderboard: Track quiz performance and compare scores with others.
Dark Mode and Sound Settings: Users can toggle between dark mode and manage sound preferences.

Design/Windows
Main Menu: Contains options to start a new game, continue a previous game, access the leaderboard, and modify settings.
Quiz Interface: Displays questions and answers clearly. Users can navigate through questions via a 'Next' button.
Settings: Users can change language preferences, toggle dark mode, adjust sound settings, and log out.

Firebase:
Firebase is utilized for:
Authentication: User accounts are handled using Firebase Authentication.
Firestore Database: Game state and leaderboard management are implemented using Firestore, allowing users to save their progress and view their results in real-time.

Quiz API
The app fetches quiz questions using the Open Trivia Database (OpenTDB) API. https://opentdb.com/api_config.php
The questions are categorized based on the user’s selected preferences, such as category and difficulty.
The API returns questions, along with correct and incorrect answers, which are then presented to the user.

Utilization of GitHub
Version Control
The project is version-controlled using Git and hosted on GitHub. The repository tracks all changes made to the project files, and version control allows developers to collaborate effectively by contributing to the codebase.

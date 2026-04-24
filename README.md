# Quiz Leaderboard System - SRM Internship Task

## Project Overview
This project is a Java-based backend application developed for the Bajaj Finserv Health JAVA Qualifier at SRMIST. The system consumes quiz event data from a validator API, deduplicates the records in real-time, and generates an accurate leaderboard.

The challenge simulates a real-world distributed system problem where the same API response data may be delivered multiple times across different polls.

## Features
* **Sequential Polling**: Executes exactly 10 API polls (0-9) to ensure all required data is received.
* **Network Resilience**: Maintains a mandatory **5-second delay** between requests to follow system constraints.
* **Deduplication Logic**: Uses a `HashSet` to track unique keys consisting of `roundId + participant`. If a duplicate entry is detected in later polls, it is ignored to prevent score inflation.
* **Leaderboard Generation**: Aggregates scores per participant and sorts the final list by `totalScore`.
* **One-Time Submission**: Submits the final calculated results in a single POST request to the `/submit` endpoint.

## How It Works
1.  **Poll**: The app loops through poll indices 0-9 using registration number **RA2311026010211**.
2.  **Filter**: For every event, the code creates a unique string: `roundId_participant`.
3.  **Validate**: 
    * If the string is new, the score is added to the participant's total.
    * If the string exists, the entry is skipped[cite: 65].
4.  **Wait**: A `Thread.sleep(5000)` command ensures the 5-second interval is maintained.
5.  **Submit**: The final sorted leaderboard is sent as a JSON payload.

## Tech Stack
* **Language**: Java 22
* **Build Tool**: Maven
* **Library**: Jackson Databind (for JSON processing)

## Submission Details
* **Name**: Yukta Bhardwaj
* **Reg No**: RA2311026010211
* **University**: SRM Institute of Science and Technology, Kattankulathur

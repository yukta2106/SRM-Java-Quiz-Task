# SRM-Java-Quiz-Task
This Java application automates quiz data collection from a distributed API for SRMIST. It executes 10 polls with a mandatory 5-second delay to handle network constraints. By using a HashSet for deduplication (roundId + participant), it ensures score accuracy before submitting a sorted leaderboard .

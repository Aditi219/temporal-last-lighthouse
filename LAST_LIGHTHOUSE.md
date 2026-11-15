# The Last Lighthouse

A tiny interactive escape room powered by Temporal Java. A storm has darkened the lighthouse. Solve three riddles to relight the beacon before the storm timer expires.

## Play in the browser (recommended)

1. Keep the local Temporal service running (`temporal server start-dev`).
2. Start the lighthouse worker in one terminal:

   ```sh
   mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseWorkerMain
   ```

3. Start the local game UI in another terminal:

   ```sh
   mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseWebMain
   ```

4. Open [http://localhost:8080](http://localhost:8080), enter a name, and use the answer and hint buttons. The page displays the current riddle and timer, and keeps the workflow ID for you.

The web server binds to localhost only. Keep both the Temporal service and worker running while you play.

## Play from the command line

## Start a game

Keep your local Temporal service running (`temporal server start-dev`). Open a terminal in this project directory and start the dedicated game worker:

```sh
mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseWorkerMain
```

In another terminal, start an expedition:

```sh
mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.StartLighthouseMain
```

Copy the `lighthouse-...` workflow ID printed by the starter. Check the current riddle with:

```sh
mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseActionMain -Dexec.args="status lighthouse-PASTE-ID"
```

Send an answer:

```sh
mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseActionMain -Dexec.args="answer lighthouse-PASTE-ID lens"
```

Ask for a hint at any time:

```sh
mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseActionMain -Dexec.args="hint lighthouse-PASTE-ID"
```

The answers are `lens`, `key`, and `west`. Each room has a ten-minute durable timer. A room timeout ends that expedition; start a new workflow ID to play again.

## What to watch in Temporal Web UI

Open `http://localhost:8233` and inspect the workflow ID. Signals (`answer` and `hint`) appear in its history. The two activities (`prepareRoom` and `restoreBeacon`) intentionally fail on their first attempt and succeed on retry. Stop the lighthouse worker with Control-C while a room is waiting, submit an answer, then restart the worker: the signal and timer are retained by Temporal, and the game continues.

The game worker uses a separate `lighthouse-quest` task queue, so it can run alongside the order demo's Java worker without competing for tasks.

# The Last Lighthouse

An interactive escape room built with Java and Temporal. A storm has darkened the coast. Solve the keeper's riddles, bring the generator online, and guide a ship safely home.

This project is designed to make Temporal's core ideas visible while you play:

- **Workflows** coordinate the expedition and preserve its state.
- **Activities** prepare rooms and restore the beacon; each deliberately fails once so you can see Temporal retry it.
- **Signals** carry player answers and hint requests into the running workflow.
- **Queries** let the UI read the current story status.
- **Durable timers** give each room ten minutes, including across worker restarts.

## Play in the browser

### Prerequisites

- Java 17 or newer
- Maven 3.8 or newer
- Temporal CLI

Install the [Temporal CLI](https://docs.temporal.io/cli#install-and-configure) if needed. The first Maven run downloads the Java SDK and build plugins.

### Run

Open three terminal tabs in the project directory.

1. Start the local Temporal service:

   ```sh
   temporal server start-dev
   ```

   Its Web UI is at `http://localhost:8233`.

2. Start the lighthouse worker and leave it running:

   ```sh
   mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseWorkerMain
   ```

3. Start the local game UI:

   ```sh
   mvn compile exec:java -Dexec.mainClass=learning.temporal.demo.lighthouse.LighthouseWebMain
   ```

4. Open [http://localhost:8080](http://localhost:8080), enter a name, and play. Use the answer and hint buttons; the page manages workflow IDs for you.

The service, worker, and UI server each run in their own terminal. Stop one with Control-C. If you stop the worker during a game, the workflow keeps its history and resumes when the worker restarts.

More gameplay notes and command-line controls are in [LAST_LIGHTHOUSE.md](LAST_LIGHTHOUSE.md).

## Explore Temporal

Open a game in `http://localhost:8233` and inspect **Event History**. Look for the failed and retried room activities, player answer/hint signals, workflow tasks, and timer events. Stop the worker while a room is waiting, send an answer from the UI, then restart the worker to observe replay and recovery.

The browser UI is served only on localhost. It does not need an external account or API key.

## Order fulfillment example

The project also includes a smaller command-line workflow covering activity retries and a human approval signal. See the `OrderWorkflow` classes in `src/main/java/learning/temporal/demo/`.

## License

MIT. See [LICENSE](LICENSE).

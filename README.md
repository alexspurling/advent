This is a branch of the main advent project to demonstrate a concurrency bug.

To compile locally, run:

```
onyx build advent.onyx -o advent.wasm -r js --multi-threaded
```

To run the demo, start a local server either using the VSCode Live Preview plugin or by running:

```
npm install -g live-server

live-server .
```

Then navigate to http://localhost:8080

importScripts("wasmmodule.js");

let wasmInstance;
let memory;

onmessage = async (e) => {
    if (e.data.msg === "init") {
        console.log("Init");
        memory = e.data.memory;
        wasmInstance = await loadWasmInstance("advent.wasm", memory);
        initOnyx(wasmInstance); // Don't forget this otherwise you'll get some weird behaviour
        console.log("Finished init", wasmInstance);
    } else if (e.data.msg === "description") {
        const day = e.data.day;
        const onyxStr = wasmInstance.exports.describe(day);
        console.log("Got description str for day", day, onyxStr);
        const description = getOnyxString(memory, onyxStr);
        postMessage({msg: "description", value: description});
    } else if (e.data.msg === "solve") {
        const day = e.data.day;
        const part = e.data.part;
        const startTime = new Date().getTime();
        console.log("Calling solve for day", day, "part", part);
        const result = getOnyxString(memory, wasmInstance.exports.solve(day, part));
        console.log("Result: ", result, "in " + (new Date().getTime() - startTime) + "ms");
        postMessage({msg: "result", value: result});
    } else {
        console.log("Received unknown message", e.data);
    }
};
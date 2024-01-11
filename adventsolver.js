importScripts("wasmmodule.js");

let wasmInstance;
let memory;

onmessage = async (e) => {
    if (e.data.msg === "init") {
        memory = e.data.memory;
        wasmInstance = await loadWasmInstance("advent.wasm", memory);
        // This only needs to be called once for a given instance of shared memory but don't forget this otherwise nothing will work
        initOnyx(wasmInstance);
    } else if (e.data.msg === "description") {
        const day = e.data.day;
        const onyxStr = wasmInstance.exports.describe(day);
        const description = getOnyxString(memory, onyxStr);
        postMessage({msg: "description", value: description});
    } else if (e.data.msg === "solve") {
        const day = e.data.day;
        const part = e.data.part;
        const hasVisualisation = e.data.hasVisualisation;
        const startTime = new Date().getTime();
        const result = getOnyxString(memory, wasmInstance.exports.solve(day, part));
        console.log("Result: ", result, "in " + (new Date().getTime() - startTime) + "ms");
        postMessage({msg: "result", value: result, hasVisualisation});
    } else {
        console.log("Received unknown message", e.data);
    }
};
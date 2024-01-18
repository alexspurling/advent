importScripts("wasmmodule.js");

let solverWasmInstance;
let solverMemory;

onmessage = async (e) => {
    if (e.data.msg === "init") {
        solverMemory = e.data.memory;
        solverWasmInstance = await loadWasmInstance("advent.wasm", solverMemory, 0);
        // This must be called before any calls to Onyx's exported functions are made
        // Initialise the Onyx heap and other things
        solverWasmInstance.exports._initialize();
        postMessage({msg: "initialised"});
    } else if (e.data.msg === "solve") {
        const startTime = new Date().getTime();
        const result = solverWasmInstance.exports.solve();
        console.log("Result:", result, "in " + (new Date().getTime() - startTime) + "ms");
        postMessage({msg: "result", value: result});
    } else {
        console.log("Received unknown message", e.data);
    }
};
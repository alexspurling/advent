importScripts("wasmmodule.js");

let wasmInstance;
let memory;

onmessage = async (e) => {
    if (e.data.msg === "init") {
        memory = e.data.memory;
        wasmInstance = await loadWasmInstance("advent.wasm", memory);
        // This only needs to be called once for a given instance of shared memory but don't forget this otherwise nothing will work
        // initOnyx(wasmInstance);

        const canvasRef = {
            canvasSize: wasmInstance.exports.getCanvasSize(),
            canvasPointer: wasmInstance.exports.getCanvasPointer()
        };
        postMessage({msg: "canvas", canvasRef});
    } else if (e.data.msg === "render") {
        const day = e.data.day;
        const part = e.data.part;
        wasmInstance.exports.render(day, part);
        postMessage({msg: "rendered", day, part});
    } else {
        console.log("Received unknown message", e.data);
    }
};
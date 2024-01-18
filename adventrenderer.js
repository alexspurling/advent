importScripts("wasmmodule.js");

let renderWasmInstance;
let renderMemory;

onmessage = async (e) => {
    if (e.data.msg === "init") {
        renderMemory = e.data.memory;
        renderWasmInstance = await loadWasmInstance("advent.wasm", renderMemory, 1);
        renderWasmInstance.exports._initialize();

        // const canvasRef = {
        //     canvasSize: renderWasmInstance.exports.getCanvasSize(),
        //     canvasPointer: renderWasmInstance.exports.getCanvasPointer()
        // };
        postMessage({msg: "initialised"});
    } else if (e.data.msg === "render") {
        renderWasmInstance.exports.render();
        postMessage({msg: "rendered"});
    } else {
        console.log("Received unknown message", e.data);
    }
};
importScripts("wasmmodule.js");

let renderWasmInstance;
let renderMemory;

onmessage = async (e) => {
    if (e.data.msg === "init") {
        renderMemory = e.data.memory;
        renderWasmInstance = await loadWasmInstance("advent.wasm", renderMemory);
        // TODO figure out where / when this should be called. If both threads call it, then only the last one will be able to called printf()
        // initOnyx(wasmInstance);
        renderWasmInstance.exports._initialize();

        const canvasRef = {
            canvasSize: renderWasmInstance.exports.getCanvasSize(),
            canvasPointer: renderWasmInstance.exports.getCanvasPointer()
        };
        postMessage({msg: "initialised", canvasRef});
    } else if (e.data.msg === "render") {
        const day = e.data.day;
        const part = e.data.part;
        renderWasmInstance.exports.render(day, part);
        postMessage({msg: "rendered", day, part});
    } else if (e.data.msg === "reset") {
        const day = e.data.day;
        renderWasmInstance.exports.reset(day);
    } else {
        console.log("Received unknown message", e.data);
    }
};
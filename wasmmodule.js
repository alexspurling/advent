let wasmModule;

const decodeOnyxString = (memory, ptr, len) => {
    let v = new DataView(memory.buffer);

    let s = "";
    for (let i = 0; i < len; i++) {
        s += String.fromCharCode(v.getUint8(ptr + i));
    }

    return s;
}

const logOnyxString = (memory, ptr, len) => {
    const stringToLog = decodeOnyxString(memory, ptr, len);
    console.log(performance.now(), stringToLog.trim());
    wasmModule.instance.exports.printCallback();
}

const getOnyxString = (memory, str) => {
    let view = new DataView(memory.buffer);
    let strptr = view.getUint32(str, true);
    let strlen = view.getUint32(str + 4, true);
    return decodeOnyxString(memory, strptr, strlen);
}

const onyxKillThread = () => {
    console.log("Worker thread killed");
}

const loadWasmModule = async (wasmModuleUrl, memory) => {
    let importObject = {
        host: {
            print_str: (ptr, len) => logOnyxString(memory, ptr, len),
            time: Date.now,
            kill_thread: onyxKillThread,
            progress: (p) => {
                postMessage({msg: "progress", value: p});
            }
        },
        onyx: {
            memory: memory
        }
    };

    wasmModule = await WebAssembly.instantiateStreaming(
        fetch(wasmModuleUrl),
        importObject
    );
    return wasmModule;
}

const loadWasmInstance = async (wasmModuleUrl, memory) => {
    return (await loadWasmModule(wasmModuleUrl, memory)).instance;
}

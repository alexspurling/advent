const decodeOnyxString = (memory, ptr, len) => {
    let v = new DataView(memory.buffer);

    let s = "";
    for (let i = 0; i < len; i++) {
        s += String.fromCharCode(v.getUint8(ptr + i));
    }

    return s;
  }

const logOnyxString = (memory, ptr, len) => {
    console.log(decodeOnyxString(memory, ptr, len));
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

    return await WebAssembly.instantiateStreaming(
        fetch(wasmModuleUrl),
        importObject
    );
}

const loadWasmInstance = async (wasmModuleUrl, memory) => {
    let wasmModule = await loadWasmModule(wasmModuleUrl, memory);
    return wasmModule.instance;
}

const initOnyx = (wasmInstance) => {
    // Initialise the Onyx heap and other things
    wasmInstance.exports._initialize();
}

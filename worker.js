console.log("Running worker");

let wasmInstance = undefined;

const onyx_decode_text = (ptr, len) => {
    let v = new DataView(wasmInstance.exports.memory.buffer);

    let s = "";
    for (let i = 0; i < len; i++) {
        s += String.fromCharCode(v.getUint8(ptr + i));
    }

    return s;
}

const onyx_print_str = (ptr, len) => {
    console.log(onyx_decode_text(ptr, len));
}

const onyx_get_str = (str) => {
    let view = new DataView(wasmInstance.exports.memory.buffer);
    let strptr = view.getUint32(str, true);
    let strlen = view.getUint32(str + 4, true);

    return onyx_decode_text(strptr, strlen);
}

const importWasmModule = async (wasmModuleUrl) => {
    let importObject = {
        host: {
            print_str: onyx_print_str,
            time: Date.now,
            progress: (p) => {
                console.log("Progress: ", p);
                postMessage({msg: "progress", value: p});
            }
        }
    };

    return await WebAssembly.instantiateStreaming(
        fetch(wasmModuleUrl),
        importObject
    );
}

const loadWasm = async () => {
    console.log("Loading wasm module");
    wasmModule = await importWasmModule("./advent.wasm");
    console.log("Loaded wasm module", wasmModule);

    // Initialise the Onyx runtime - this is needed to set up heap space and other things
    wasmModule.instance.exports._initialize();
    wasmInstance = wasmModule.instance;
}

// Create a Uint8Array to give us access to Wasm Memory
// const inputByteArray = stringToByteArray("Hello world");
// const inputPointer = wasmInstance.exports.__alloc(inputByteArray.length);
// const wasmMemory = new Uint8Array(wasmModule.instance.exports.memory);
// wasmMemory.set(inputByteArray, inputPointer);

// function stringToByteArray(str) {
//     const byteArray = new Uint8Array(str.length);
//     for (let i = 0; i < str.length; i++) {
//       byteArray[i] = str.charCodeAt(i);
//     }
//     return byteArray;
// }

loadWasm();

onmessage = (e) => {
    if (e.data.msg == "description") {
        const day = e.data.params[0];
        const description = onyx_get_str(wasmModule.instance.exports.describe(day));
        postMessage({msg: "description", value: description});
    } else if (e.data.msg == "solve") {
        const day = e.data.params[0];
        const part = e.data.params[1];
        const startTime = new Date().getTime();
        const result = wasmModule.instance.exports.solve(day, part);
        console.log("Result: ", result, "in " + (new Date().getTime() - startTime) + "ms");
        postMessage({msg: "result", value: result});
    } else {
        console.log("Received unknown message", e.data);
    }
};
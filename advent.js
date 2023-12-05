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

function stringToByteArray(str) {
    const byteArray = new Uint8Array(str.length);
  
    for (let i = 0; i < str.length; i++) {
      byteArray[i] = str.charCodeAt(i);
    }
  
    return byteArray;
}

const openWindow = (day) => {
    // console.log("You opened window ", windowNum);

    // Create a Uint8Array to give us access to Wasm Memory
    // const inputByteArray = stringToByteArray("Hello world");
    // const inputPointer = wasmInstance.exports.__alloc(inputByteArray.length);
    // const wasmMemory = new Uint8Array(wasmModule.instance.exports.memory);
    // wasmMemory.set(inputByteArray, inputPointer);
    

    document.getElementById("day").innerHTML = "Day " + day;
    
    const description = get_onyx_str(wasmModule.instance.exports.describe(day));

    document.getElementById("description").innerHTML = description;
    
    const windowDiv = document.getElementById("window");
    windowDiv.style.opacity = 0.75;
}

const solve = (day) => {
    const result = wasmModule.instance.exports.solve(day, 1);
    console.log("Result: ", result);
}

window.onload = function () {
    loadWasm();
}

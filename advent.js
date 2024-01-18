let solver;
let renderer;

let memory;

let canvasRef;
let solved = false;
let finalFrameRequested = false;

window.onload = function () {
    initSolver();
    initRenderer();

    // First initialise the solver - the render worker will be initialised after the solver has finished initialising
    solver.postMessage({msg: "init", memory});
}


function initSolver() {
    solver = new Worker("adventsolver.js");
    solver.onmessage = (e) => {
        if (e.data.msg === "initialised") {
            console.log("Solver initialised");

            // Now initialise the renderer worker
            renderer.postMessage({msg: "init", memory});
        } else if (e.data.msg == "description") {
            document.getElementById("description").innerHTML = e.data.value;
        } else if (e.data.msg == "progress") {
            const progress = e.data.value;
            document.getElementById("result").innerHTML = "(progress: " + progress.toFixed(1) + "%)";
        } else if (e.data.msg == "result") {
            document.getElementById("result").innerHTML = e.data.value;
            solved = true;
        } else {
            console.log("Received unexpected result from worker", e);
        }
    };
    // Initialise the shared memory object
    memory = new WebAssembly.Memory({
        initial: 1024,
        maximum: 1024,
        shared: true
      });
    wasmByteMemoryArray = new Uint8Array(memory.buffer);
}

function initRenderer() {
    renderer = new Worker("adventrenderer.js");
    renderer.onmessage = (e) => {
        if (e.data.msg === "initialised") {
            // console.log("Got canvas ref", e.data.canvasRef);
            // canvasRef = e.data.canvasRef;
        } else if (e.data.msg === "rendered") {
            // Request to draw the current frame in shared memory on the next animation frame
            requestAnimationFrame(() => drawCanvas());
            if (solved) {
                finalFrameRequested = true;
            }
        }
    }
}

function render() {
    // Ask the render worker to render a frame into shared memory
    renderer.postMessage({msg: "render"});
}

function drawCanvas() {
    // const canvasData = wasmByteMemoryArray.slice(canvasRef.canvasPointer, canvasRef.canvasPointer + canvasRef.canvasSize);
    // const canvas = document.getElementById("solutioncanvas");
    // const ctx = canvas.getContext("2d");
    // const imageData = ctx.createImageData(canvas.width, canvas.height);
    // imageData.data.set(canvasData);
    // ctx.putImageData(imageData, 0, 0);

    if (!finalFrameRequested) {
        render();
    }
}

const closeWindow = () => {
    document.getElementById("window").style.display = "none";
    return false;
}

const solve = () => {
    solved = false;
    finalFrameRequested = false;
    render();
    document.getElementById("resultsection").style.display = "inline";
    document.getElementById("result").innerHTML = "Working..."

    solver.postMessage({msg: "solve"});
    return false;
}

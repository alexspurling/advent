let worker = undefined;

window.onload = function () {
    worker = new Worker("worker.js");
    worker.onmessage = (e) => {
        if (e.data.msg == "description") {
            document.getElementById("description").innerHTML = e.data.value;
        } else if (e.data.msg == "progress") {
            const progress = e.data.value;
            document.getElementById("result").innerHTML = "(progress: " + progress.toFixed(1) + "%)";
        } else if (e.data.msg == "result") {
            document.getElementById("result").innerHTML = e.data.value;
        } else {
            console.log("Received unexpected result from worker", e);
        }
    };
}

const openWindow = (day) => {
    document.getElementById("day").innerHTML = "Day " + day;
    document.getElementById("part1").onclick = () => {return solve(day, 1)};
    document.getElementById("part2").onclick = () => {return solve(day, 2)};

    document.getElementById("resultsection").style.display = "none";

    worker.postMessage({msg: "description", params: [day]});

    const windowDiv = document.getElementById("window");
    windowDiv.style.opacity = 0.75;

    return false;
}

const solve = (day, part) => {
    document.getElementById("resultsection").style.display = "block";
    document.getElementById("result").innerHTML = "Working..."

    worker.postMessage({msg: "solve", params: [day, part]});

    return false;
}

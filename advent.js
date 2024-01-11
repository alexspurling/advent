let solver = undefined;
let particles = [];

window.onload = function () {
    solver = new Worker("adventsolver.js");
    solver.onmessage = (e) => {
        if (e.data.msg == "description") {
            document.getElementById("description").innerHTML = e.data.value;
        } else if (e.data.msg == "progress") {
            const progress = e.data.value;
            document.getElementById("canvasresult").innerHTML = "(progress: " + progress.toFixed(1) + "%)";
            document.getElementById("normalresult").innerHTML = "(progress: " + progress.toFixed(1) + "%)";
        } else if (e.data.msg == "result") {
            if (e.data.hasVisualisation) {
                document.getElementById("canvasresult").innerHTML = e.data.value;
            } else {
                document.getElementById("normalresult").innerHTML = e.data.value;
            }
        } else {
            console.log("Received unexpected result from worker", e);
        }
    };

    initOnyx();
    initSnow();
}

function initOnyx() {
    const memory = new WebAssembly.Memory({
        initial: 1024,
        maximum: 1024,
        shared: true
      });
    solver.postMessage({msg: "init", memory});
}


let frameCounter = 0;

function initSolutionCanvas() {
    let ctx = document.getElementById("solutioncanvas").getContext("2d");
    let renderFps = () => {
        ctx.clearRect(0, 0, 750, 750);
        ctx.font = "20px sans";
        ctx.fillStyle = "black";
        ctx.fillText("Frames: " + frameCounter, 20, 40);
        frameCounter += 1;
        requestAnimationFrame(renderFps);
    };
    requestAnimationFrame(renderFps);
}

const openWindow = (day, hasVisualisation) => {
    document.getElementById("day").innerHTML = "Day " + day;
    document.getElementById("part1").onclick = () => {return solve(day, 1, hasVisualisation)};
    document.getElementById("part2").onclick = () => {return solve(day, 2, hasVisualisation)};

    document.getElementById("normalresultsection").style.display = "none";
    document.getElementById("canvasresultsection").style.display = "none";

    solver.postMessage({msg: "description", day});

    document.getElementById("window").style.display = "block";

    return false;
}

const closeWindow = () => {
    document.getElementById("window").style.display = "none";

    return false;
}

const solve = (day, part, hasVisualisation) => {
    if (hasVisualisation) {
        document.getElementById("solutioncontainer").style.display = "block";
        document.getElementById("canvasresultsection").style.display = "block";
        document.getElementById("canvasresult").innerHTML = "Working..."
    } else {
        document.getElementById("solutioncontainer").style.display = "none";
        document.getElementById("normalresultsection").style.display = "block";
        document.getElementById("normalresult").innerHTML = "Working..."
    }

    solver.postMessage({msg: "solve", day, part, hasVisualisation});

    return false;
}

function initSnow() {
	//canvas init
	var canvas = document.getElementById("snowcanvas");
	var ctx = canvas.getContext("2d");

	//canvas dimensions
	var W = window.innerWidth;
	var H = window.innerHeight;
	// canvas.width = W;
	// canvas.height = H;

	//snowflake particles
	var mp = 100; //max particles
	for(var i = 0; i < mp; i++)
	{
		particles.push({
			x: Math.random()*W, // x-coordinate
			y: Math.random()*H, // y-coordinate
			r: Math.random()*3+1, // radius
			d: Math.random()*mp, // density
            sway: Math.random() // How much the particle is affected by the "wind"
		})
	}

	requestAnimationFrame(() => draw(ctx));
}


// Lets draw the flakes
function draw(ctx)
{
    const W = ctx.canvas.clientWidth;
    const H = ctx.canvas.clientHeight;
    ctx.clearRect(0, 0, W, H);

    ctx.fillStyle = "rgba(255, 255, 255, 0.8)";
    ctx.beginPath();
    for (var i = 0; i < particles.length; i++)
    {
        var p = particles[i];
        ctx.moveTo(p.x, p.y);
        ctx.arc(p.x, p.y, p.r, 0, Math.PI*2, true);
    }
    ctx.fill();
    update(W, H);
    requestAnimationFrame(() => draw(ctx));
}

// Function to move the snowflakes
// angle will be an ongoing incremental flag. Sin and Cos functions will be applied to it to create vertical and horizontal movements of the flakes
var angle = 0;
function update(W, H)
{
    angle += 0.01;
    for (var i = 0; i < particles.length; i++)
    {
        var p = particles[i];
        // Updating X and Y coordinates
        // We will add 1 to the cos function to prevent negative values which will lead flakes to move upwards
        // Every particle has its own density which can be used to make the downward movement different for each flake
        // Lets make it more random by adding in the radius
        p.y += (Math.cos(angle + p.d) + 1 + p.r/2) * 0.5;
        p.x += Math.sin(angle) * p.sway;

        // Sending flakes back from the top when it exits
        // Lets make it a bit more organic and let flakes enter from the left and right also.
        if (p.x > W+5 || p.x < -5 || p.y > H)
        {
            if (i%3 > 0) // 66.67% of the flakes
            {
                particles[i] = {x: Math.random() * W, y: -10, r: p.r, d: p.d, sway: p.sway};
            }
            else
            {
                // If the flake is exitting from the right
                if (Math.sin(angle) > 0)
                {
                    // Enter from the left
                    particles[i] = {x: -5, y: Math.random() * H, r: p.r, d: p.d, sway: p.sway};
                }
                else
                {
                    // Enter from the right
                    particles[i] = {x: W+5, y: Math.random() * H, r: p.r, d: p.d, sway: p.sway};
                }
            }
        }
    }
}
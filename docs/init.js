const script = document.getElementById("script");
const output = document.getElementById('output');
const error = document.getElementById('error');

const handler = {
    println: (message) => {
        error.classList.add("d-none");
        error.innerHTML = "";
        output.innerHTML += message + "<br/>";
    },
    onError: (message) => {
        error.classList.remove("d-none");
        error.innerHTML = message;
    }
}

const lox = new klox.klox.Lox(handler);

const execute = () => {
    output.innerHTML = "";
    lox.reset();
    lox.run(script.value);
}

document.getElementById("run").addEventListener("click", execute)
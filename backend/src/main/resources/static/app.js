const button = document.getElementById('run-check');
const output = document.getElementById('output');

button.addEventListener('click', runCheck);

async function runCheck() {
    button.disabled = true;
    try {
        const response = await fetch('/api/checks', {method: 'POST'});
        if (!response.ok) {
            output.textContent = 'API error: ' + response.status;
            return;
        }
        const result = await response.json();
        output.textContent = JSON.stringify(result, null, 2);
    } catch (error) {
        output.textContent = 'Backend not reachable';
    } finally {
        button.disabled = false;
    }
}
const image = document.querySelector('#item-image');
const loadingIcon = document.querySelector('#loading-icon');
const options = document.querySelector('#options');
const feedback = document.querySelector('#feedback');
const nextButton = document.querySelector('#next');
let question;
let busy = false;

async function post(path, body) {
    const response = await fetch(path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    if (!response.ok) {
        throw new Error(response.status === 409
            ? 'This question is no longer active. Get a new question.'
            : 'Could not reach the game. Check that the server is running and try again.');
    }
    return response.json();
}

function showError(error) {
    feedback.className = 'feedback error';
    feedback.textContent = error.message;
    nextButton.textContent = 'Get a new question';
    nextButton.hidden = false;
}

async function newQuestion() {
    if (busy) return;
    busy = true;
    nextButton.hidden = true;
    options.replaceChildren();
    image.hidden = true;
    loadingIcon.hidden = false;
    feedback.className = 'feedback';
    feedback.textContent = 'Loading a question…';
    try {
        question = await post('/api/quiz/questions');
        // Wait for the icon before enabling the answers.
        await new Promise((resolve, reject) => {
            image.onload = resolve;
            image.onerror = () => reject(new Error('The image could not be loaded. Try a new question.'));
            image.src = question.iconUrl;
        });
        image.hidden = false;
        loadingIcon.hidden = true;
        image.alt = 'The Minecraft item to identify';
        for (const option of question.options) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'option';
            button.textContent = option.name;
            button.dataset.optionId = option.id;
            button.addEventListener('click', () => submitAnswer(option.id));
            options.append(button);
        }
        feedback.textContent = 'Which name matches the picture?';
        options.querySelector('button').focus({ preventScroll: true });
    } catch (error) {
        showError(error);
    } finally {
        busy = false;
    }
}

async function submitAnswer(optionId) {
    if (busy) return;
    busy = true;
    const buttons = [...options.querySelectorAll('button')];
    buttons.forEach(button => button.disabled = true);
    feedback.textContent = 'Checking your answer…';
    try {
        const result = await post('/api/quiz/answers', { questionId: question.id, optionId });
        feedback.className = result.correct ? 'feedback success' : 'feedback error';
        feedback.textContent = result.correct
            ? `Correct! It is ${result.correctName}.`
            : `Not quite! The correct answer is ${result.correctName}.`;
        image.alt = result.correctName;
        for (const button of buttons) {
            if (button.dataset.optionId === result.correctOptionId) button.classList.add('correct');
            else if (button.dataset.optionId === optionId) button.classList.add('wrong');
        }
        nextButton.textContent = 'Next question →';
        nextButton.hidden = false;
        nextButton.focus({ preventScroll: true });
    } catch (error) {
        showError(error);
    } finally {
        busy = false;
    }
}

nextButton.addEventListener('click', newQuestion);
newQuestion();

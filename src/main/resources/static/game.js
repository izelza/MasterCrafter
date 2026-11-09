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
            ? 'Frågan är inte längre aktiv. Hämta en ny fråga.'
            : 'Kunde inte kontakta spelet. Kontrollera att servern körs och försök igen.');
    }
    return response.json();
}

function showError(error) {
    feedback.className = 'feedback error';
    feedback.textContent = error.message;
    nextButton.textContent = 'Hämta ny fråga';
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
    feedback.textContent = 'Hämtar en fråga…';
    try {
        question = await post('/api/quiz/questions');
        // Vänta på ikonen innan det går att svara.
        await new Promise((resolve, reject) => {
            image.onload = resolve;
            image.onerror = () => reject(new Error('Bilden kunde inte laddas. Prova en ny fråga.'));
            image.src = question.iconUrl;
        });
        image.hidden = false;
        loadingIcon.hidden = true;
        image.alt = 'Minecraft-föremålet du ska namnge';
        for (const option of question.options) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'option';
            button.textContent = option.name;
            button.dataset.optionId = option.id;
            button.addEventListener('click', () => submitAnswer(option.id));
            options.append(button);
        }
        feedback.textContent = 'Vilket namn hör till bilden?';
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
    feedback.textContent = 'Kontrollerar svaret…';
    try {
        const result = await post('/api/quiz/answers', { questionId: question.id, optionId });
        feedback.className = result.correct ? 'feedback success' : 'feedback error';
        feedback.textContent = result.correct
            ? `Rätt! Det är ${result.correctName}.`
            : `Inte riktigt! Rätt svar är ${result.correctName}.`;
        image.alt = result.correctName;
        for (const button of buttons) {
            if (button.dataset.optionId === result.correctOptionId) button.classList.add('correct');
            else if (button.dataset.optionId === optionId) button.classList.add('wrong');
        }
        nextButton.textContent = 'Nästa fråga →';
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

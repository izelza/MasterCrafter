package com.bursie;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {
    private static final String CURRENT_QUESTION = "quiz.currentQuestion";
    private final List<ItemCatalog.Item> items;

    public QuizController(ItemCatalog catalog) {
        items = catalog.items();
    }

    @PostMapping("/questions")
    public Question newQuestion(HttpSession session) {
        var random = ThreadLocalRandom.current();
        var correct = items.get(random.nextInt(items.size()));
        ItemCatalog.Item incorrect;
        do {
            incorrect = items.get(random.nextInt(items.size()));
        } while (incorrect.name().equals(correct.name()));

        var rightOption = new Option(UUID.randomUUID().toString(), correct.name());
        var options = new ArrayList<>(List.of(rightOption,
                new Option(UUID.randomUUID().toString(), incorrect.name())));
        Collections.shuffle(options);
        var question = new Question(UUID.randomUUID().toString(), correct.iconUrl(), List.copyOf(options));
        synchronized (session) {
            session.setAttribute(CURRENT_QUESTION,
                    new ActiveQuestion(question, rightOption.id(), correct.name()));
        }
        return question;
    }

    @PostMapping("/answers")
    public AnswerResult answer(@RequestBody Answer answer, HttpSession session) {
        synchronized (session) {
            var active = (ActiveQuestion) session.getAttribute(CURRENT_QUESTION);
            if (active == null || !active.question().id().equals(answer.questionId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This question is no longer active. Get a new question.");
            }
            if (active.question().options().stream().noneMatch(option -> option.id().equals(answer.optionId()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose one of the available answers.");
            }
            session.removeAttribute(CURRENT_QUESTION);
            return new AnswerResult(active.correctOptionId().equals(answer.optionId()),
                    active.correctName(), active.correctOptionId());
        }
    }

    public record Option(String id, String name) {}
    public record Question(String id, String iconUrl, List<Option> options) {}
    public record Answer(String questionId, String optionId) {}
    public record AnswerResult(boolean correct, String correctName, String correctOptionId) {}
    private record ActiveQuestion(Question question, String correctOptionId, String correctName) {}
}

package com.ll.kotudy.word.service;

import com.ll.kotudy.util.exception.AppException;
import com.ll.kotudy.util.exception.ErrorCode;
import com.ll.kotudy.word.domain.MyWordRepository;
import com.ll.kotudy.word.dto.QuizWordDto;
import com.ll.kotudy.word.dto.QuizForm;
import com.ll.kotudy.word.dto.response.QuizResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuizService {

    private final MyWordRepository myWordRepository;

    public QuizResponse createForm() {
        List<QuizWordDto> quizWordDtoList = myWordRepository.findMyWordDistinctRandomForQuiz();
        checkEnoughMyWordToCreateQuiz(quizWordDtoList);
        List<QuizForm> quizFormList = new ArrayList<>();

        quizFormList = IntStream.range(0, quizWordDtoList.size() / 4)
                .mapToObj(i -> createOneQuizForm(quizWordDtoList.subList(i * 4, Math.min((i + 1) * 4, quizWordDtoList.size()))))
                .collect(Collectors.toList());

        return new QuizResponse("나만의 단어장을 기반으로 생성된 퀴즈입니다.", quizFormList);
    }

    private void checkEnoughMyWordToCreateQuiz(List<QuizWordDto> quizWordDtoList) {
        if (quizWordDtoList.size() < 40) {
            throw new AppException(ErrorCode.NOT_ENOUGH_MYWORD_FOR_CREATE_QUIZ, "퀴즈를 만들기에 충분한 단어가 없습니다.");
        }
    }

    private QuizForm createOneQuizForm (List<QuizWordDto> subQuizList) {
        QuizForm createdQuizForm = new QuizForm();
        String answerWord = subQuizList.get(0).getName();

        List<String> choices = shuffleChoices(subQuizList);

        createdQuizForm.setQuestion(subQuizList.get(0).getMean());
        createdQuizForm.setAnswerIndex(choices.indexOf(answerWord) + 1);
        createdQuizForm.setChoices(choices);

        return createdQuizForm;
    }

    private List<String> shuffleChoices(List<QuizWordDto> subQuizList) {
        List<String> choices = subQuizList.stream()
                .map(QuizWordDto::getName)
                .collect(Collectors.toList());

        Collections.shuffle(choices);

        return choices;
    }
}
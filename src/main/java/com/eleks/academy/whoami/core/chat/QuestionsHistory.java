package com.eleks.academy.whoami.core.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class QuestionsHistory {

    private String player;

    private String type;

    private String question;

    private List<AnswersHistory> answers = new ArrayList<>();

    public QuestionsHistory(String player,String type, String question) {
        this.player = player;
        this.type = type;
        this.question = question;
    }

    public void addAnswer(String player, String answer) {
        this.answers.add(new AnswersHistory(player, answer));
    }

}

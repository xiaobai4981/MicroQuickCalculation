package io.github.subhamtyagi.quickcalculation.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Question {
    private final ArrayList<Integer> options = new ArrayList<>(4);
    private final String question;
    private final int correctAnswer;

    Question(String question, int correctAnswer, Boolean ignoreNegative) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        options.add(correctAnswer);
        Random r = new Random();
        if (correctAnswer > 40) {
            boolean b = r.nextBoolean();
            boolean c = r.nextBoolean();
            if (b) {
                options.add(correctAnswer + 30);
                options.add(correctAnswer + 10);
                options.add(correctAnswer + 20);

            } else if (c) {
                options.add(correctAnswer - 30);
                options.add(correctAnswer + 10);
                options.add(correctAnswer - 20);
            } else {
                options.add(correctAnswer - 30);
                options.add(correctAnswer - 10);
                options.add(correctAnswer - 20);
            }
        } else if (correctAnswer > 20) {
            while (options.size() < 4) {
                int offset = r.nextInt(15) + 5;
                int wrongAnswer = r.nextBoolean() ? correctAnswer + offset : correctAnswer - offset;
                if (ignoreNegative && wrongAnswer < 0) continue;
                if (!options.contains(wrongAnswer)) {
                    options.add(wrongAnswer);
                }
            }

        } else if (correctAnswer > 10) {
            while (options.size() < 4) {
                int offset = r.nextInt(8) + 5;
                int wrongAnswer = r.nextBoolean() ? correctAnswer + offset : correctAnswer - offset;
                if (ignoreNegative && wrongAnswer < 1) continue;
                if (!options.contains(wrongAnswer)) {
                    options.add(wrongAnswer);
                }
            }
        } else if (correctAnswer < -30) {
            boolean b = r.nextBoolean();
            boolean c = r.nextBoolean();
            if (b) {
                options.add(correctAnswer + 30);
                options.add(correctAnswer + 10);
                options.add(correctAnswer + 20);

            } else if (c) {
                options.add(correctAnswer - 30);
                options.add(correctAnswer + 10);
                options.add(correctAnswer - 20);
            } else {
                options.add(correctAnswer - 30);
                options.add(correctAnswer - 10);
                options.add(correctAnswer - 20);
            }
        } else {
            while (options.size() < 4) {
                int offset = r.nextInt(3) + 5;
                int wrongAnswer = r.nextBoolean() ? correctAnswer + offset : correctAnswer - offset;
                if (ignoreNegative && wrongAnswer < 1) continue;
                if (!options.contains(wrongAnswer)) {
                    options.add(wrongAnswer);
                }
            }
        }
        Collections.shuffle(options);

    }

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return options.get(0).toString();
    }

    public String getOption2() {
        return options.get(1).toString();
    }

    public String getOption3() {
        return options.get(2).toString();
    }

    public String getOption4() {
        return options.get(3).toString();
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }


}

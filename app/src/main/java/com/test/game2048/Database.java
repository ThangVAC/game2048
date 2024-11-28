package com.test.game2048;

import android.content.Context;
import android.content.SharedPreferences;

public class Database {
    private static final String PREFS_NAME = "Game2048Prefs";
    private static final String KEY_HIGH_SCORE = "HighScore";
    private SharedPreferences sharedPreferences;

    public Database(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveHighScore(int score) {
        int currentHighScore = getHighScore();
        if (score > currentHighScore) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_HIGH_SCORE, score);
            editor.apply();
        }
    }

    public int getHighScore() {
        return sharedPreferences.getInt(KEY_HIGH_SCORE, 0);
    }
}

package com.test.game2048;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Play2048 extends AppCompatActivity {
    private GameView gameView;
    private TextView scoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play2048);

        scoreView = findViewById(R.id.scoreView);
        gameView = findViewById(R.id.gameView);

        gameView.setScoreView(scoreView);
    }

    public void onGameOver(int score) {
        Database db = new Database(this);
        db.saveHighScore(score);

        setContentView(R.layout.game_over);
        TextView finalScore = findViewById(R.id.finalScore);
        finalScore.setText(String.valueOf(score));
    }
}

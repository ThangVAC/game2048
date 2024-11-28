package com.test.game2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
    private static final int GRID_SIZE = 4; // Kích thước lưới 4x4
    private static final int TILE_PADDING = 10;
    private static final int TILE_COLOR = Color.LTGRAY;
    private static final int EMPTY_TILE_COLOR = Color.DKGRAY;

    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private Paint tilePaint, textPaint;
    private int score = 0;

    private float startX, startY; // Điểm bắt đầu chạm
    private TextView scoreView; // View hiển thị điểm

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        tilePaint = new Paint();
        tilePaint.setAntiAlias(true);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(64);
        resetGame();
    }

    public void setScoreView(TextView scoreView) {
        this.scoreView = scoreView;
    }

    private void resetGame() {
        score = 0;
        grid = new int[GRID_SIZE][GRID_SIZE];
        addRandomTile();
        addRandomTile();
        invalidate();
    }

    private void addRandomTile() {
        List<int[]> emptyTiles = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 0) {
                    emptyTiles.add(new int[]{i, j});
                }
            }
        }
        if (!emptyTiles.isEmpty()) {
            int[] randomTile = emptyTiles.get((int) (Math.random() * emptyTiles.size()));
            grid[randomTile[0]][randomTile[1]] = Math.random() < 0.9 ? 2 : 4;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int tileSize = Math.min(width, height) / GRID_SIZE;

        // Vẽ ô lưới
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int value = grid[i][j];
                int left = j * tileSize + TILE_PADDING;
                int top = i * tileSize + TILE_PADDING;
                int right = (j + 1) * tileSize - TILE_PADDING;
                int bottom = (i + 1) * tileSize - TILE_PADDING;

                tilePaint.setColor(value == 0 ? EMPTY_TILE_COLOR : TILE_COLOR);
                canvas.drawRect(left, top, right, bottom, tilePaint);

                // Vẽ số
                if (value != 0) {
                    String text = String.valueOf(value);
                    float textWidth = textPaint.measureText(text);
                    float textX = left + (tileSize - textWidth) / 2;
                    float textY = top + (tileSize - textPaint.descent() - textPaint.ascent()) / 2;
                    canvas.drawText(text, textX, textY, textPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                handleSwipe(endX - startX, endY - startY);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleSwipe(float dx, float dy) {
        boolean moved = false;
        if (Math.abs(dx) > Math.abs(dy)) {
            // Swipe ngang
            if (dx > 0) {
                moved = moveRight();
            } else {
                moved = moveLeft();
            }
        } else {
            // Swipe dọc
            if (dy > 0) {
                moved = moveDown();
            } else {
                moved = moveUp();
            }
        }

        if (moved) {
            addRandomTile();
            updateScore();
            invalidate();

            if (isGameOver()) {
                ((Play2048) getContext()).onGameOver(score);
            }
        }
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = grid[i];
            int[] newRow = mergeTiles(row);
            if (!equalArrays(row, newRow)) {
                moved = true;
                grid[i] = newRow;
            }
        }
        return moved;
    }

    private boolean moveRight() {
        boolean moved = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = reverseArray(grid[i]);
            int[] newRow = mergeTiles(row);
            newRow = reverseArray(newRow);
            if (!equalArrays(grid[i], newRow)) {
                moved = true;
                grid[i] = newRow;
            }
        }
        return moved;
    }

    private boolean moveUp() {
        boolean moved = false;
        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = getColumn(j);
            int[] newColumn = mergeTiles(column);
            if (!equalArrays(column, newColumn)) {
                moved = true;
                setColumn(j, newColumn);
            }
        }
        return moved;
    }

    private boolean moveDown() {
        boolean moved = false;
        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = reverseArray(getColumn(j));
            int[] newColumn = mergeTiles(column);
            newColumn = reverseArray(newColumn);
            if (!equalArrays(getColumn(j), newColumn)) {
                moved = true;
                setColumn(j, newColumn);
            }
        }
        return moved;
    }

    private int[] mergeTiles(int[] tiles) {
        List<Integer> merged = new ArrayList<>();
        boolean mergedThisTurn = false;

        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] == 0) continue;

            if (!mergedThisTurn && !merged.isEmpty() && merged.get(merged.size() - 1) == tiles[i]) {
                merged.set(merged.size() - 1, tiles[i] * 2);
                score += tiles[i] * 2;
                mergedThisTurn = true;
            } else {
                merged.add(tiles[i]);
                mergedThisTurn = false;
            }
        }

        while (merged.size() < tiles.length) {
            merged.add(0);
        }

        return merged.stream().mapToInt(i -> i).toArray();
    }

    private int[] getColumn(int colIndex) {
        int[] column = new int[GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            column[i] = grid[i][colIndex];
        }
        return column;
    }

    private void setColumn(int colIndex, int[] column) {
        for (int i = 0; i < GRID_SIZE; i++) {
            grid[i][colIndex] = column[i];
        }
    }

    private int[] reverseArray(int[] array) {
        int[] reversed = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            reversed[i] = array[array.length - 1 - i];
        }
        return reversed;
    }

    private boolean equalArrays(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isGameOver() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 0) return false;
                if (j < GRID_SIZE - 1 && grid[i][j] == grid[i][j + 1]) return false;
                if (i < GRID_SIZE - 1 && grid[i][j] == grid[i + 1][j]) return false;
            }
        }
        return true;
    }

    private void updateScore() {
        if (scoreView != null) {
            scoreView.setText("Score: " + score);
        }
    }
}

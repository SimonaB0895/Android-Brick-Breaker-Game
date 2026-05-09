package com.example.brickbrakergame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {
    Context context;
    ArrayList<Ball> balls = new ArrayList<>();// Списък с активните топки
    Handler handler;// Управлява графика на обновяване
    final long UPDATE_MILLIS = 16;// Интервал за опресняване (~60 FPS)
    Runnable runnable;// Кодът, който се изпълнява при всеки кадър


    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    Paint brickPaint = new Paint();

    // Координати, резултат и брой животи
    float paddleX, paddleY;
    int points = 0;
    int life = 3;

    // Графични ресурси и размери на екрана
    Bitmap ballBitmap, paddle, bonusBitmap;
    int dWidth, dHeight;
    int ballWidth, ballHeight;

    // Логика и параметри на бонус системата
    float bonusX, bonusY;
    boolean bonusActive = false;
    boolean bonusAlreadySpawned = false;
    int bonusSpeed = 12;

    // Аудио ресурси
    SoundPool soundPool;
    int soundHit, soundMiss, soundBreak;

    // Игрови обекти и прогрес
    Random random;
    Brick[] bricks = new Brick[30];
    int numBricks = 0;
    int brokenBricks = 0;
    boolean gameOver = false;

    public GameView(Context context) {
        super(context);
        this.context = context;

        // Звукови ефекти
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundHit = soundPool.load(context, R.raw.hit, 1);
        soundMiss = soundPool.load(context, R.raw.missing, 1);
        soundBreak = soundPool.load(context, R.raw.hitbrick, 1);

        // Изображения
        ballBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        paddle = BitmapFactory.decodeResource(getResources(), R.drawable.paddle);
        bonusBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bonus);

        ballWidth = ballBitmap.getWidth();
        ballHeight = ballBitmap.getHeight();

        // Дизайн на тухлите
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        brickPaint.setColor(Color.parseColor("#1e51de"));
        brickPaint.setStyle(Paint.Style.FILL);

        // Вземане на размерите на екрана
        dWidth = context.getResources().getDisplayMetrics().widthPixels;
        dHeight = context.getResources().getDisplayMetrics().heightPixels;

        random = new Random();

        // Позициониране на paddle
        paddleY = (dHeight * 4) / 5;
        paddleX = dWidth / 2 - paddle.getWidth() / 2;

        // Задаване на координати на топката -
        // на случайно място по ширината на екрана и на 1/3 от височината на екрана и скорост
        balls.add(new Ball(random.nextInt(dWidth - ballWidth), dHeight / 3, new Velocity(15, 18)));

        createBricks();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();

                if (!gameOver) {
                    handler.postDelayed(this, 16);// Ако играта не е приключила, планира следващото обновяване след 16ms
                }
            }
        };
        handler.post(runnable);
    }

    private void createBricks() {
        //задаване на размери на мрежата от тухли
        int brickWidth = dWidth / 8;
        int brickHeight = dHeight / 16;
        numBricks = 0;
        //създаване на тухлите
        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 3; row++) {
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dWidth == 0 || dHeight == 0) {
            dWidth = canvas.getWidth();
            dHeight = canvas.getHeight();
            return;
        }
        canvas.drawColor(Color.parseColor("#2a1b3e"));
        if (gameOver) return;// Ако играта е приключила, спираме по-нататъшното чертане на обекти

        // Рисуване на тухлите
        for (int j = 0; j < numBricks; j++) {
            // Изчисляване на координатите на правоъгълника за всяка тухла
            if (bricks[j].getVisibility()) {
                int left = bricks[j].column * bricks[j].width;
                int top = bricks[j].row * bricks[j].height;
                int right = left + bricks[j].width;
                int bottom = top + bricks[j].height;
                canvas.drawRect(left + 3, top + 3, right - 3, bottom - 3, brickPaint);
            }
        }

        //  Логика за движение на бонуса
        if (bonusActive) {
            bonusY += bonusSpeed;
            canvas.drawBitmap(bonusBitmap, bonusX, bonusY, null);

            // Проверка за сблъсък между бонуса и хилката
            if (bonusX + bonusBitmap.getWidth() >= paddleX && bonusX <= paddleX + paddle.getWidth() &&
                    bonusY + bonusBitmap.getHeight() >= paddleY && bonusY <= paddleY + paddle.getHeight()) {

                // Втора топка, зададена в различна посока
                balls.add(new Ball(paddleX + paddle.getWidth() / 2, paddleY - 60, new Velocity(20, -18)));
                bonusActive = false;
            }
            if (bonusY > dHeight) bonusActive = false;
        }

        // Движение и сблъсъци на топките
        for (int i = 0; i < balls.size(); i++) {
            Ball b = balls.get(i);
            // Актуализиране на позицията чрез добавяне на текущата скорост
            b.x += b.velocity.getX();
            b.y += b.velocity.getY();

            // Стени
            if (b.x >= dWidth - ballWidth || b.x <= 0) b.velocity.setX(b.velocity.getX() * -1);
            if (b.y <= 0) b.velocity.setY(Math.abs(b.velocity.getY()));//осигурява движение надолу
            // чрез превръщане на вертикалната скорост в положителна стойност

            // Изпускане
            if (b.y > paddleY + paddle.getHeight()) {
                if (balls.size() > 1) {// Премахване на допълнителната топка без загуба на живот
                    balls.remove(i);
                    i--;
                    continue;
                } else {// Загуба на живот при последната топка
                    soundPool.play(soundMiss, 1, 1, 0, 0, 1);
                    life--;
                    if (life <= 0) {
                        gameOver = true;
                        launchGameOver();
                        return;
                    } else {
                        // Рестартиране на позицията на основната топка
                        b.x = random.nextInt(dWidth - ballWidth);
                        b.y = dHeight / 3;
                        b.velocity.setY(18);
                    }
                }
            }

            // Удар в paddle
            if (b.x + ballWidth >= paddleX && b.x <= paddleX + paddle.getWidth() &&
                    b.y + ballHeight >= paddleY && b.y <= paddleY + paddle.getHeight()) {
                soundPool.play(soundHit, 0.4f, 0.4f, 0, 0, 1);
                b.y = paddleY - ballHeight - 5;

                // Промяна на посоката спрямо точката на удар
                float hitPoint = (b.x + ballWidth / 2) - paddleX;
                b.velocity.setX((int)((hitPoint - paddle.getWidth()/2) / 4));//Промяна на Х скорост спрямо мястото на сблъсъка, позволявайки на играча да насочва топката
                // Увеличаване на скорост Y след всеки удар за прогресивно нарастваща трудност
                int currentSpeedY = Math.abs(b.velocity.getY());
                if (currentSpeedY < 25) {
                    b.velocity.setY(-(currentSpeedY + 1));
                } else {
                    b.velocity.setY(-25);
                }
            }

            // Сблъсък с тухли
            for (int j = 0; j < numBricks; j++) {
                if (bricks[j].getVisibility()) {
                    // Изчисляване на границите на тухлата за проверка за допир
                    int left = bricks[j].column * bricks[j].width;
                    int top = bricks[j].row * bricks[j].height;
                    int right = left + bricks[j].width;
                    int bottom = top + bricks[j].height;

                    // Проверка за удар
                    if (b.x + ballWidth >= left && b.x <= right && b.y <= bottom && b.y + ballHeight >= top) {

                        soundPool.play(soundBreak, 1, 1, 0, 0, 1);
                        bricks[j].setInvisible();
                        points += 10;
                        brokenBricks++;

                        // Обръща посоката на движение по вертикала (отскок)
                        b.velocity.setY(b.velocity.getY() * -1);
                        // Избутва топката извън обекта след удар, за да не се заклещи в него
                        if (b.velocity.getY() > 0) b.y = bottom + 1;// Премества я под обекта, ако пада надолу
                        else b.y = top - ballHeight - 1;// Премества я над обекта, ако лети нагоре

                        // Логика за бонуса
                        if (brokenBricks == 10 && !bonusAlreadySpawned) {
                            bonusActive = true;
                            bonusAlreadySpawned = true;
                            bonusX = left;
                            bonusY = top;
                        }

                        // Проверка за победа - всички тухлички са ударени
                        if (brokenBricks >= numBricks) {
                            gameOver = true;
                            launchGameOver();
                            return; // Спираме веднага
                        }
                        break;
                    }
                }
            }
            // Изчертаване на топката на новата Ѝ позиция
            canvas.drawBitmap(ballBitmap, b.x, b.y, null);
        }

// Визуализиране на хилката и текущия резултат
        canvas.drawBitmap(paddle, paddleX, paddleY, null);
        canvas.drawText("Score: " + points, 50, 120, textPaint);
        // Лента за наличните животи: променя цвета си на червен, когато остане 1 живот
        healthPaint.setColor(life > 1 ? Color.GREEN : Color.RED);
        canvas.drawRect(dWidth - 300, 50, dWidth - 300 + (80 * life), 90, healthPaint);
// Ако играта не е приключила, планира следващото преначертаване
        if (!gameOver) handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    private void launchGameOver() {
        handler.removeCallbacksAndMessages(null); // Спиране на всички бъдещи прерисувания
        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("points", points);
        context.startActivity(intent);
        ((Activity)context).finish(); // Затваряне на текущата игра
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Проверява дали потребителят е натиснал или движи пръста си по екрана
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            paddleX = event.getX() - paddle.getWidth() / 2;// Центрира хилката спрямо позицията на пръста
            // Ограничава движението на хилката в рамките на екрана
            if (paddleX < 0) paddleX = 0;
            if (paddleX > dWidth - paddle.getWidth()) paddleX = dWidth - paddle.getWidth();
        }
        return true;
    }
}
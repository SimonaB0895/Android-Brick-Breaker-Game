package com.example.brickbrakergame;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
/*MainActivity управлява първоначалното зареждане
и стартирането на игровия процес.*/

public class MainActivity extends AppCompatActivity {
//Методът onCreate се извиква при стартиране на приложението
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Зареждане на графичния интерфейс на началното меню чрез XML файла activity_main
        setContentView(R.layout.activity_main);
        // Поддържане на екрана включен по време на игра
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //Стартира играта при натискане на бутона Play
    // Преминава от началното меню към игровия екран
        public void startGame(View view){
           GameView gameView = new GameView(this);
           setContentView(gameView);//активира игровия цикъл
        }
}
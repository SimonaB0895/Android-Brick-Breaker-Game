package com.example.brickbrakergame;

import android.content.Intent;
import android.os. Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget. ImageView;
import android.widget.TextView;

import androidx.annotation. Nullable;
import androidx.appcompat.app.AppCompatActivity;
/*
 Клас GameOver управлява екрана, който се появява при завършване на играта.
  Той показва финалния резултат и предоставя опции за рестарт или изход. */

public class GameOver extends AppCompatActivity {
    TextView tvPoints;
    ImageView ivNewHighest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Зареждане на XML оформлението за край на играта
        setContentView(R.layout.game_over);

        ivNewHighest = findViewById(R.id.ivNewHeighest);
        tvPoints = findViewById(R.id.tvPoints);

// Извличане на данните, изпратени от GameView чрез Intent
        if (getIntent().getExtras() != null) {
            int points = getIntent().getExtras().getInt("points");

            tvPoints.setText("" + points);
            //при събрани 240 точки, се показва специално изображение
            if (points >= 240) {
                ivNewHighest.setVisibility(View.VISIBLE);
            }
        }
    }
    public void restart(View view){
        //Създаване на Intent за преход обратно към главното меню
        Intent intent = new Intent(GameOver.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exit(View view) {
        finish();
    }
}

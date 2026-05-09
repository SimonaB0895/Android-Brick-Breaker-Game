package com.example.brickbrakergame;
// Клас, съхраняващ физическите параметри и състоянието на топката
public class Ball {
    float x, y;
    Velocity velocity;// обект, съхраняващ текущата скорост и посока на движение
    //инициализиране на позицията и скоростта на топката
    public Ball(float x, float y, Velocity velocity) {
        this.x = x;
        this.y = y;
        this.velocity = velocity;
    }
}

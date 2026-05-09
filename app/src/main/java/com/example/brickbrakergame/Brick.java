/*package com.example.brickbrakergame;

public class Brick {
    private boolean isVisible;
    public int row, column, width, height;

    public Brick(int row, int column, int width, int height){
        isVisible = true;
        this.row = row;
        this.column = column;
        this.width = width;
        this.height = height;

    }
    public void setInvisible() {
        isVisible = false;
    }
    public boolean getVisibility() {
        return isVisible;
    }


}*/
package com.example.brickbrakergame;
//Клас, дефиниращ свойствата и състоянието на отделна тухла
public class Brick {
    private boolean isVisible;
    public int row, column, width, height;

    public Brick(int row, int column, int width, int height) {
        this.isVisible = true;
        this.row = row;
        this.column = column;
        this.width = width;
        this.height = height;
    }

    // Маркира тухлата като разбита (невидима)
    public void setInvisible() { isVisible = false; }
    // Връща текущото състояние на тухлата
    public boolean getVisibility() { return isVisible; }
}

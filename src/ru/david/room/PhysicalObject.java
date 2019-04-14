package ru.david.room;

import java.io.Serializable;

/**
 * Сериализуемый физический объект с координатами, шириной и высотой
 */
class PhysicalObject implements Serializable {
    private int x, y, width, height;

    /**
     * Создаёт физический объект с указанными координатами
     * @param x x-координата
     * @param y y-координата
     */
    PhysicalObject(int x, int y) {
        this(x, y, 0, 0);
    }

    /**
     * Создаёт физический объект с указанными координатами, шириной и высотой
     * @param x x-координата
     * @param y y-координата
     * @param width ширина
     * @param height высота
     */
    PhysicalObject(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
    }

    /**
     * Устанавливает координаты, ширину и высоту объекта
     * @param x x-координата
     * @param y y-координата
     * @param width ширина
     * @param height высота
     */
    private void setBounds(int x, int y, int width, int height) {
        setPosition(x, y);
        setSize(width, height);
    }

    /**
     * Устанавливает координаты объекта
     * @param x x-координата
     * @param y y-координата
     */
    private void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    /**
     * Устанавливает размер объекта
     * @param width ширина
     * @param height высота
     */
    private void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    private void setX(int x) { this.x = x; }
    private void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setWidth(int width) {
        if (width < 0)
            throw new IllegalArgumentException("Ширина не может быть отрицательной");
        this.width = width;
    }

    public void setHeight(int height) {
        if (height < 0)
            throw new IllegalArgumentException("Высота не может быть отрицательной");
        this.height = height;
    }
}

package ru.david.room;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Creature extends PhysicalObject implements Comparable<Creature> {
    private String name = "Безымянный";
    private Date createdDate = new Date();

    Creature(int x, int y) {
        super(x, y);
    }

    Creature(int x, int y, String name) {
        super(x, y);
        this.name = name;
    }

    Creature(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    Creature(int x, int y, int width, int height, String name) {
        super(x, y, width, height);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return String.format("Существо по имени %s, координаты: (%s; %s), размер: %s x %s, создано %s",
                name,
                getX(),
                getY(),
                getWidth(),
                getHeight(),
                new SimpleDateFormat("hh:mm aa, dd.MM.yyyy").format(createdDate)
        );
    }

    public int getCoolness() {
        return getX() + getY();
    }

    @Override
    public int compareTo(Creature o) {
        return getCoolness() - o.getCoolness();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) return false;
        if (this == o) return true;
        Creature c = (Creature) o;
        return  c.getName().equals(getName()) &&
                c.getX() == getX() &&
                c.getY() == getY() &&
                c.getWidth() == getWidth() &&
                c.getHeight() == getHeight();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getX(), getY(), getWidth(), getHeight());
    }
}

package ru.david.room;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Тюряа для плохих пацанов!
 */
public class Hoosegow {
    private PriorityBlockingQueue<Creature> collection = new PriorityBlockingQueue<>();
    private Date createdDate = new Date();
    private boolean edited = false;

    /**
     * Добавляет существо в тюрягу
     * @param creature существо, которое надо добавить
     */
    public void add(Creature creature) {
        collection.add(creature);
        edited = true;
    }

    /**
     * Удаляет существо из тюряги
     * @param creature существо, которое надо удалить
     * @return true, если удаление произошло
     */
    public boolean remove(Creature creature) {
        if (!collection.contains(creature))
            return false;
        collection.remove(creature);
        return true;
    }

    /**
     * Удаляет каждое существо, если оно круче, чем указанное
     * @param creature существо, с которым произоёдет сравнение
     * @return количество удалённых существ
     */
    public int removeGreaterThan(Creature creature) {
        int sizeBefore = collection.size();
        collection.stream()
                .filter(current -> current.compareTo(creature) > 0)
                .forEach(current -> collection.remove(current));
        int removed = sizeBefore - collection.size();
        if (removed > 0)
            edited = true;
        return removed;
    }

    /**
     * Удаляет последнее существо тюряги. Если тюряга пуста, возвращает null.
     * @return удалённое существо
     */
    public Creature removeLast() {
        PriorityBlockingQueue<Creature> NewQueue = new PriorityBlockingQueue<>();
        while(collection.size() > 1)
            NewQueue.add(collection.poll());

        Creature removed = collection.poll();
        collection = NewQueue;
        edited = removed != null;
        return removed;
    }

    /**
     * @return коллекцию существ
     */
    public PriorityBlockingQueue<Creature> getCollection() {
        return collection;
    }

    public int getSize() {
        return collection.size();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
        edited = true;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    /**
     * Очищает коллекцию
     */
    public void clear() {
        collection.clear();
    }

    /**
     * @return читабельное строковое представление тюряги
     */
    public String getCollectionInfo() {
        return "Тюряга, содержит существ в коллекции типа " + collection.getClass().getName() + ",\n" +
                "создана " + createdDate + ",\n" +
                "содержит " + collection.size() + " существ";
    }
}

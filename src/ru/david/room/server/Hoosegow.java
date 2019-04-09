package ru.david.room.server;

import ru.david.room.Creature;

import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Тюряа для плохих пацанов!
 */
public class Hoosegow {
    private PriorityBlockingQueue<Creature> collection = new PriorityBlockingQueue<>();
    private Date createdDate = new Date();
    private boolean edited = false;
    private static int maxCollectionElements = 256;

    /**
     * Добавляет существо в тюрягу
     * @param creature существо, которое надо добавить
     */
    void add(Creature creature) {
        if (collection.size() >= maxCollectionElements)
            throw new HoosegowOverflowException();
        collection.add(creature);
        edited = true;
    }

    /**
     * Удаляет существо из тюряги
     * @param creature Существо, которое надо удалить
     * @return True, если удаление произошло
     */
    boolean remove(Creature creature) {
        if (!collection.contains(creature))
            return false;
        collection.remove(creature);
        return true;
    }

    /**
     * Удаляет каждое существо, если оно круче, чем указанное
     * @param creature Существо, с которым произоёдет сравнение
     * @return Количество удалённых существ
     */
    int removeGreaterThan(Creature creature) {
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
     * @return Удалённое существо
     */
    Creature removeLast() {
        PriorityBlockingQueue<Creature> NewQueue = new PriorityBlockingQueue<>();
        while(collection.size() > 1)
            NewQueue.add(collection.poll());

        Creature removed = collection.poll();
        collection = NewQueue;
        edited = removed != null;
        return removed;
    }

    /**
     * @return Коллекцию существ
     */
    PriorityBlockingQueue<Creature> getCollection() {
        return collection;
    }

    /**
     * @return Размер коллекции
     */
    int getSize() {
        return collection.size();
    }

    /**
     * Устанавливает время создания тюряги
     * @param createdDate Время создания тюряги
     */
    void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
        edited = true;
    }

    /**
     * Возвращает время создания тюряги
     * @return Время создания тюряги
     */
    Date getCreatedDate() {
        return createdDate;
    }

    /**
     * @return True, если тюряга изменилась
     */
    public boolean isEdited() {
        return edited;
    }

    /**
     * Устанавливает факт изменённости тюряги. Установите значение true,
     * чтобы обозначить, что тюряга была изменена
     * @param edited факт изменйнности тюряги
     */
    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    /**
     * Очищает коллекцию
     */
    void clear() {
        collection.clear();
    }

    /**
     * @return Максимальное количество существ, которые могут поместиться в тюрягу
     */
    static int getMaxCollectionElements() {
        return maxCollectionElements;
    }

    /**
     * Задаёт максимально количество существ, которые поместятся в тюрягу
     * @param maxCollectionElements количество существ
     */
    static void setMaxCollectionElements(int maxCollectionElements) {
        Hoosegow.maxCollectionElements = maxCollectionElements;
    }

    /**
     * @return Количество свободного пространства в тюряге
     */
    public int getAvailableSpace() {
        return maxCollectionElements - this.getSize();
    }

    /**
     * @return читабельное строковое представление тюряги
     */
    String getCollectionInfo() {
        return "Тюряга, содержит существ в коллекции типа " + collection.getClass().getName() + ",\n" +
                "создана " + createdDate + ",\n" +
                "содержит " + collection.size() + " существ";
    }
}

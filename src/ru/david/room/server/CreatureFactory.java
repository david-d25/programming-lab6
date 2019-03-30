package ru.david.room.server;

import ru.david.room.json.*;

public class CreatureFactory {
    static Creature makeCreatureFromJSON(String json) throws Exception {
        JSONEntity entity;

        try {
            entity = JSONParser.parse(json);
        } catch (JSONParseException e) {
            throw new JSONParseException("Не удалось обработать этот json:\n" + e);
        }

        if (entity == null)
            throw new IllegalArgumentException("Требуется json-объект, но получен null");

        JSONObject object = entity.toObject(new IllegalArgumentException("Нужен json-объект, но вместо него " + entity.getTypeName()));

        JSONEntity x = object.getItem("x");
        JSONEntity y = object.getItem("y");

        if (x == null)
            throw new IllegalArgumentException("Требуется параметр 'x', но он не указан");
        if (y == null)
            throw new IllegalArgumentException("Требуется параметр 'y', но он не указан");

        Creature creature = new Creature(
                x.toInt(new IllegalArgumentException("Параметр 'x' должен быть числом, но это " + x.getTypeName())),
                y.toInt(new IllegalArgumentException("Параметр 'y' должен быть числом, но это " + y.getTypeName())));

        JSONEntity width = object.getItem("width");
        if (width != null)
            creature.setWidth(width.toInt(new IllegalArgumentException("Указанная ширина должна быть числом, но это " + width.getTypeName())));

        JSONEntity height = object.getItem("height");
        if (height != null)
            creature.setHeight(height.toInt(new IllegalArgumentException("Указанная вцысота должна быть числом, но это " + height.getTypeName())));

        JSONEntity name = object.getItem("name");
        if (name != null)
            creature.setName(name.toString(new IllegalArgumentException("Указанное имя должно быть строкой, но это " + name.getTypeName())).getContent());

        return creature;
    }
}

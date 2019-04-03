package ru.david.room;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Date;

public class HoosegowStateController {
    /**
     * Загружает состояние тюряги из указанной строки. Строка должна содержать состояние в формате XML.
     * Обратите внимание, что имеющиеся существа не удаляются из тюряги.
     * @param hoosegow тюряга, в которую надо загрузить состояние
     * @param xml строка, содержащая состояние в формате XML
     * @throws ParserConfigurationException Если произойдёт ошибка парсинга
     * @throws IOException Если произойдёт ошиька ввода-вывода
     * @throws SAXException Если произойдёт какая-то другая ошибка парсинга
     */
    public static void loadState(Hoosegow hoosegow, String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element root = document.getDocumentElement();

        NodeList nodes = root.getElementsByTagName("timestamp");
        if (nodes.getLength() > 0) {
            try {
                if (hasChildElements(nodes.item(0)))
                    throw new SAXException("<timestamp> не должен иметь дочерних нодов");
                else
                    hoosegow.setCreatedDate(new Date(Long.parseLong(nodes.item(0).getTextContent())));
            } catch (NumberFormatException e) {
                throw new SAXException("<timestamp> должен хранить целое число");
            }
        }

        nodes = root.getElementsByTagName("creature");
        for (int a = 0; a < nodes.getLength(); a++) {
            NodeList current = nodes.item(a).getChildNodes();

            Integer x = null, y = null, width = null, height = null;
            Long created = null;
            String name = null;

            for (int b = 0; b < current.getLength(); b++) {
                Node property = current.item(b);
                if (hasChildElements(property))
                    throw new SAXException("Дочерние элементы в <creature> не должны иметь своих дочерних элементов");

                switch (property.getNodeName()) {
                    case "x":
                        try {
                            x = Integer.parseInt(property.getTextContent());
                        } catch (NumberFormatException e) {
                            throw new SAXException("<x> в <creature> должен хранить целое число");
                        }
                        break;
                    case "y":
                        try {
                            y = Integer.parseInt(property.getTextContent());
                        } catch (NumberFormatException e) {
                            throw new SAXException("<y> в <creature> должен хранить целое число");
                        }
                        break;
                    case "created":
                        try {
                            created = Long.parseLong(property.getTextContent());
                        } catch (NumberFormatException e) {
                            throw new SAXException("<created> в <creature> должен хранить целое число (long)");
                        }
                        break;
                    case "width":
                        try {
                            width = Integer.parseInt(property.getTextContent());
                        } catch (NumberFormatException e) {
                            throw new SAXException("<width> в <creature> должен хранить целое число");
                        }
                        break;
                    case "height":
                        try {
                            height = Integer.parseInt(property.getTextContent());
                        } catch (NumberFormatException e) {
                            throw new SAXException("<height> в <creature> должен хранить целое число");
                        }
                        break;
                    case "name":
                        name = property.getTextContent();
                        break;
                }
            }
            if (x == null)
                throw new SAXException("<x> обязателен, но не указан в <creature>");
            if (y == null)
                throw new SAXException("<y> обязателен, но не указан в <creature>");

            Creature creature = new Creature(x, y);
            if (width != null)
                creature.setWidth(width);
            if (height != null)
                creature.setHeight(height);
            if (name != null)
                creature.setName(name);
            if (created != null)
                creature.setCreatedDate(new Date(created));

            hoosegow.add(creature);
        }
    }

    /**
     * Возвращает true, если элемент XML имеет внутри ещё элементы
     * @param el элемент XML
     * @return true, если указанный элемент содержит ещё элементы
     */
    private static boolean hasChildElements(Node el) {
        NodeList children = el.getChildNodes();
        for (int i = 0;i < children.getLength();i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                return true;
        }
        return false;
    }

    /**
     * Сохраняет состояние тюряги в поток в формате XML
     * @param hoosegow тюряга, состояние которой надо сохранить
     * @param writer поток, в который будет записано состояние в формате XML
     * @throws IOException если произойдёт ошибка чтения-записи
     */
    public static void saveState(Hoosegow hoosegow, OutputStreamWriter writer) throws IOException {
        writer.write("<?xml version=\"1.0\"?>\n");
        writer.write("<state>\n");
        writer.write("  <timestamp>" + hoosegow.getCreatedDate().getTime() + "</timestamp>\n");
        for (Creature creature : hoosegow.getCollection()) {
            writer.write("  <creature>\n");
            writer.write("    <x>" + creature.getX() + "</x>\n");
            writer.write("    <y>" + creature.getY() + "</y>\n");
            writer.write("    <created>" + creature.getCreatedDate().getTime() + "</created>\n");
            writer.write("    <width>" + creature.getWidth() + "</width>\n");
            writer.write("    <height>" + creature.getHeight() + "</height>\n");
            writer.write("    <name>" + creature.getName() + "</name>\n");
            writer.write("  </creature>\n");
        }
        writer.write("</state>\n");
        writer.close();
    }
}

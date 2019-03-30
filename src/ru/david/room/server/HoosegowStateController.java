package ru.david.room.server;

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

class HoosegowStateController {
    static void loadState(Hoosegow hoosegow, String xml) throws ParserConfigurationException, IOException, SAXException {
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

            hoosegow.add(creature);
        }
    }

    public static boolean hasChildElements(Node el) {
        NodeList children = el.getChildNodes();
        for (int i = 0;i < children.getLength();i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                return true;
        }
        return false;
    }

    static void saveState(Hoosegow hoosegow, OutputStreamWriter writer) throws IOException {
        writer.write("<?xml version=\"1.0\"?>\n");
        writer.write("<state>\n");
        writer.write("  <timestamp>" + hoosegow.getCreatedDate().getTime() + "</timestamp>\n");
        for (Creature creature : hoosegow.getCollection()) {
            writer.write("  <creature>\n");
            writer.write("    <x>" + creature.getX() + "</x>\n");
            writer.write("    <y>" + creature.getY() + "</y>\n");
            writer.write("    <width>" + creature.getWidth() + "</width>\n");
            writer.write("    <height>" + creature.getHeight() + "</height>\n");
            writer.write("    <name>" + creature.getName() + "</name>\n");
            writer.write("  </creature>\n");
        }
        writer.write("</state>\n");
        writer.flush();
    }
}

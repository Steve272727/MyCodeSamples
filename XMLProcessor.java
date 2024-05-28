package com.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLProcessor {

    public static void main(String[] args) {
        //String inputFilePath = "input.txt";  // Path to your input file
        String inputFilePath = "C:\\Data\\Code\\xmlLogProcessor\\Input.txt";        

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processLine(String line) {
        try {
            String[] parts = line.split(",", 3);
            if (parts.length < 3) {
                System.err.println("Invalid input format: " + line);
                return;
            }

            String id = parts[0];
            String number = parts[1];
            String xmlContent = parts[2];

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            NodeList rows = doc.getElementsByTagName("row");
            for (int i = 0; i < rows.getLength(); i++) {
                Element row = (Element) rows.item(i);

                String r1s1 = getTagValue(row, "r1s1");
                String r1s2 = getTagValue(row, "r1s2");
                String r1s3 = getTagValue(row, "r1s3");

                System.out.printf("%s,%s,%s,%s,%s%n", id, number, r1s1, r1s2, r1s3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTagValue(Element row, String tagName) {
        NodeList fields = row.getElementsByTagName("field");
        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) fields.item(i);
            if (field.getAttribute("name").equals(tagName)) {
                return field.getTextContent();
            }
        }
        return "";
    }
}

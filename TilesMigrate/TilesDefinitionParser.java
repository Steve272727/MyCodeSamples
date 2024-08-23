import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.util.List;

public class TilesDefinitionParser {

    public static void main(String[] args) {
        try {
            // Specify the XML file path
            //File inputFile = new File("tiles-definitions.xml");

            File inputFile = new File("D:/Data/Code/apacheTilesMigrate/TilesMigrate/input/tiles-theapp.xml");
            
            // Create a SAXBuilder to parse the XML
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);

            // Get the root element (tiles-definitions)
            Element rootElement = document.getRootElement();

            // Get the list of all "definition" elements
            List<Element> definitions = rootElement.getChildren("definition");

            // Iterate through the definitions
            for (Element definition : definitions) {
                String extendsAttr = definition.getAttributeValue("extends");
                
                // Check if the "extends" attribute is present
                if (extendsAttr != null) {
                    String nameAttr = definition.getAttributeValue("name");
                    String bodyValue = null;

                    // Get the list of "put-attribute" elements
                    List<Element> putAttributes = definition.getChildren("put-attribute");

                    // Find the "put-attribute" with name "body"
                    for (Element putAttribute : putAttributes) {
                        if ("body".equals(putAttribute.getAttributeValue("name"))) {
                            bodyValue = putAttribute.getAttributeValue("value");
                            break;
                        }
                    }

                    // Output the values to the console
                    System.out.println("Definition Name: " + nameAttr);
                    System.out.println("Extends: " + extendsAttr);
                    System.out.println("Body Value: " + bodyValue);
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


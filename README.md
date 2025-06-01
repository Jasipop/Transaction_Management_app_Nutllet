# Nutllet
This is a simple transaction management app on PC. Includes basic functions like record transactions and categorization. It is equipped with qwen2:1.5b AI model based on Ollama platform. Each function page is an individual java program.
 
## Running configuration：

**Overall：**

    Java-jdk-21
    javafx-sdk-21
    jackson-2.15.3
    Ollama qwen2:1.5b

**VM option: (As reference, need to replace with your own path)** 
```
--module-path
"E:\Program Files\Java\javafx-sdk-21.0.6\lib"
--add-modules
javafx.controls,javafx.fxml,javafx.web
--add-exports
javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED
--add-exports
javafx.web/com.sun.webkit=ALL-UNNAMED
```
**Detailed jars:**
all extra jars are put aside in the `lib` folder.

    javafx.base.jar
    javafx.controls.jar
    javafx.fxml.jar
    javafx.graphics.jar
    javafx.media.jar
    javafx.properties
    javafx.swing.jar 
    javafx.web.jar
    javafx-swt.jar

    jackson-annotations-2.15.3.jar
    jackson-core-2.15.3.jar
    jackson-databind-2.15.3.jar

    ooxml-schemas-1.4.jar
    openxml4j-1.0-beta.jar
    
    poi-5.4.1.jar
    poi-ooxml-5.4.1.jar

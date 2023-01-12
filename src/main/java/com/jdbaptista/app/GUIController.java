package com.jdbaptista.app;

import com.jdbaptista.app.labor.LaborGeneratorBuilder;
import com.jdbaptista.app.labor.error.*;
import com.jdbaptista.app.material.MaterialsGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.jdbaptista.app.labor.LaborGenerator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class GUIController {
    @FXML
    private Label resultText;

    @FXML
    private Label resultText2;

    @FXML
    protected void onLaborButtonClick() throws IOException, InvalidFormatException {
        File inFile = new File("files/input/Dailies.xlsx");
        File configFile = new File("files/input/WCPercentages.xlsx");
        File salaryFile = new File("files/input/Salaries.xlsx");
        XSSFWorkbook load = new XSSFWorkbook(salaryFile); // preloads apache poi, not sure what its loading, but...
        String outFolder = "files/output";
        OutputStream outputStream = getStringOutputStream();
        // get a valid instance of LaborGenerator from builder
        LaborGenerator generator = null;
        try {
            generator = new LaborGeneratorBuilder()
                    .setInFile(inFile)
                    .setWorkCompFile(configFile)
                    .setSalaryFile(salaryFile)
                    .setOutFolder(outFolder)
                    .setOutputLog(outputStream)
                    .build();
        } catch (Exception e) {
            resultText.setText(e.getMessage());
            System.out.println(e.getStackTrace());
        }
        generator.run();
        resultText.setText(outputStream.toString());
        outputStream.close();
    }

    private OutputStream getStringOutputStream() {
        return new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b);
            }

            //Netbeans IDE automatically overrides this toString()
            public String toString() {
                return this.string.toString();
            }
        };
    }

    @FXML
    protected void onMaterialClick() {
        String inFolder = "files/input/Weekly Receipts";
        String outFile = "files/output/Materials Reports";

        MaterialsGenerator generator = new MaterialsGenerator(inFolder, outFile);
        String result = generator.run();
        resultText2.setText(result);
    }

}
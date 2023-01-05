package com.jdbaptista.app.material;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MaterialsGenerator {
    final private String inFolder;
    final private File[] inFiles;
    final private String outFile;
    public String log;
    private Formatter formatter;
    private HashMap<Integer, XSSFWorkbook> workbooks;

    public MaterialsGenerator(String inFolder, String outFile) {
        this.inFolder = inFolder;
        File folder = new File(inFolder);
        inFiles = folder.listFiles();
        this.outFile = outFile;
        this.workbooks = new HashMap<>();
        this.log = "";
    }

    public String run() {
        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            formatter = new Formatter(wb);
            OutputStream fileOut = new FileOutputStream(outFile + ".xlsx");
            ArrayList<LocalDate> weeks = new ArrayList<>();
            HashMap<LocalDate, ArrayList<Container>> containersLookup = new HashMap<>();
            for (int i = 0; i < inFiles.length; i++) {
                ArrayList<Container> containers = parseData(inFiles[i]);
                if (containers.size() == 0) continue;
                String[] partials = inFiles[i].getName().split(" ");
                String[] ending = partials[1].split("\\.")[0].split("-");
                DateTimeFormatter df = DateTimeFormatter.ofPattern("M-d-y");
                LocalDate weekDate = LocalDate.parse(ending[0] + "-" + ending[1] + "-" + ending[2], df);
                weeks.add(weekDate);
                containersLookup.put(weekDate, containers);
            }
            Collections.sort(weeks, Collections.reverseOrder());

            for (LocalDate week : weeks) {
                String header = week.getMonthValue() + "_" + week.getDayOfMonth() + "_" + week.getYear();
                Sheet sheet = wb.createSheet(header);
                writeWeek(sheet, containersLookup.get(week), "Week Ending " + header);
            }
            wb.write(fileOut);
            fileOut.close();
            wb.close();

        } catch (Exception e) {
            e.printStackTrace();
            log += "Failed to write material reports.\nInput file may be open.\n";
            return log;
        }
        log += "Generated material reports successfully.\n";
        return log;
    }

    private ArrayList<Container> parseData(File inFile) throws IOException {
        String addressCell;
        String vendorCell;
        LocalDate dateCell;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        double amountCell;
        ArrayList<Container> ret = new ArrayList<>();

        // try to open the input file.
        Workbook wb;
        Sheet sheet = null;
        try {
            if (inFile.isHidden()) return ret;
            wb = WorkbookFactory.create(inFile);
            sheet = wb.getSheetAt(0);
        } catch (IOException e) {
            log += "Something went wrong reading the input.\n";
            throw e;
        }

        // try to read the data
        if (sheet == null) {
            log += "Input file contains no data.\n";
            throw new IOException("sheet is null in file " + inFile);
        }
        for (Row row : sheet) {
            // get the row
            Iterator<Cell> cellIterator = row.cellIterator();
            try {
                addressCell = cellIterator.next().toString().strip();
                vendorCell = cellIterator.next().toString().strip();
                dateCell = LocalDate.parse(cellIterator.next().toString(), df);
                amountCell = Double.parseDouble(cellIterator.next().toString());
            } catch (Exception e) {
                log += "Something went wrong reading the receipts at row " + (row.getRowNum() + 1) + ". Skipped this row.\n";
                e.printStackTrace();
                continue;
            }
            if (addressCell == null || vendorCell == null) {
                log += "Something went wrong reading the receipts at row " + (row.getRowNum() + 1) + ". Skipped this row.\n";
                continue;
            }
            // input the row
            ret.add(new Container(addressCell, vendorCell, dateCell, amountCell));
        }
        return ret;
    }

    private void writeWeek(Sheet sheet, ArrayList<Container> containers, String header) throws IOException {
        // split the containers up into packets of similar year/month
        // order packets by the following specification for the formatter
        Container[] sorted = containers.stream()
                                        .sorted(Comparator.comparing(Container::address)
                                                .thenComparing(Container::vendor)
                                                .thenComparing(Container::date)
                                                .thenComparing(Container::amount))
                                        .toArray(Container[]::new);
        formatter.writeWeek(sheet, sorted, header);
    }
}

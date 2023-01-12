package com.jdbaptista.app.labor;

import com.jdbaptista.app.labor.error.DatedTableException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.*;

/**
 * Contains functionality to take input data stored in excel files and create or override output excel files filled
 * with business reports via the {@link LaborGenerator#run()} method. Relies on {@link LaborGeneratorBuilder} for
 * validation and initialization of input parameters.
 */
public class LaborGenerator {
    // required attributes
    final private File inFile;
    final private File workCompFile;
    final private File salaryFile;
    final private String outFolder;
    // optional attributes
    final private OutputStreamWriter outputLog;
    // static attributes
    final private HashMap<Job, XSSFWorkbook> jobs;
    final private HashMap<LocalDate, HashMap<String, Double>> salaries;

    public LaborGenerator(LaborGeneratorBuilder builder) {
        // required parameters
        this.inFile = builder.getInFile();
        this.workCompFile = builder.getWorkCompFile();
        this.salaryFile = builder.getSalaryFile();
        this.outFolder = builder.getOutFolder();

        // optional parameters
        this.outputLog = new OutputStreamWriter(builder.getOutputLog());

        // static attributes
        jobs = new HashMap<>();
        salaries = new HashMap<>();
    }

    public void run() throws IOException {
        try {
            long startTime = System.nanoTime();
            System.out.println("Loading worker comp data...");
            DatedTableData<Integer, Double> wcData = getWorkComp();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

            startTime = System.nanoTime();
            System.out.println("Loading salary data...");
            getSalaries();
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

            startTime = System.nanoTime();
            System.out.println("Loading shift data...");
            parseData();
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

            startTime = System.nanoTime();
            System.out.println("Calculating report data...");
            calculate(wcData);
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

            startTime = System.nanoTime();
            System.out.println("Generating reports...");
            generateFile();
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        outputLog.flush();
    }

    private DatedTableData<Integer, Double> getWorkComp() throws Exception {
        DatedTableData<Integer, Double> wcData = new DatedTableData<>();

        long start = System.nanoTime();
        // get proper sheet
        XSSFWorkbook wb = new XSSFWorkbook(workCompFile);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> rowIter = sheet.iterator();
        long end = System.nanoTime();
        System.out.println((end - start) / 1000000);

        start = System.nanoTime();
        // load map with wc codes
        Row codeRow = rowIter.next();
        Iterator<Cell> codeCellIter = codeRow.cellIterator();
        Cell IDCell = codeCellIter.next();
        if (!IDCell.getStringCellValue().equals("WC")) throw new IOException("WC file incorrectly formatted");
        codeCellIter.forEachRemaining((codeCell) -> {
           int code = (int) codeCell.getNumericCellValue();
           wcData.addCode(code, codeCell.getColumnIndex());
        });
        end = System.nanoTime();
        System.out.println((end - start) / 1000000);

        start = System.nanoTime();
        // load map with percentages
        while (rowIter.hasNext()) {
            Row percentageRow = rowIter.next();
            Iterator<Cell> percentageCellIter = percentageRow.cellIterator();
            LocalDate date = percentageCellIter.next().getLocalDateTimeCellValue().toLocalDate();

            while (percentageCellIter.hasNext()) {
                Cell curr = percentageCellIter.next();
                double percentage = curr.getNumericCellValue();
                wcData.addChangeByCol(curr.getColumnIndex(), percentage, date);
            }
        }
        end = System.nanoTime();
        System.out.println((end - start) / 1000000);

        return wcData;
    }

    private void getSalaries() throws Exception {
        Workbook wb = WorkbookFactory.create(salaryFile);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        // confirm the salaries file is correct.
        Row row = rowIterator.next();
        Iterator<Cell> cellIterator = row.cellIterator();
        Cell cell = cellIterator.next();
        if (!cell.toString().equals("Salaries"))
            throw new IOException("Salaries file not formatted correctly.");

        ArrayList<String> names = new ArrayList<>();
        String date;
        while(cellIterator.hasNext()) {
            cell = cellIterator.next();
            names.add(cell.toString());
        }
        String[] namesArr = names.toArray(new String[0]);
        // get values
        while (rowIterator.hasNext()) {
            ArrayList<String> values = new ArrayList<>();
            row = rowIterator.next();
            cellIterator = row.cellIterator();
            cell = cellIterator.next();
            date = cell.toString();
            if (date.equals("")) {
                break;
            }
            while (cellIterator.hasNext()) {
                cell = cellIterator.next();
                values.add(cell.toString());
                if (cell.toString() == null || cell.toString().equals("")) {
                    outputLog.write("Salary value is empty at row ");
                    outputLog.write((row.getRowNum() + 1) + " column ");
                    outputLog.write((cell.getColumnIndex() + 1) + ".\n");
                    throw new Exception("");
                }
            }
            addSalaryDate(date, namesArr, values.toArray(new String[0]));
        }
    }

    private void generateFile() throws IOException {
        try {
            for (Job job : jobs.keySet()) {
                XSSFWorkbook wb = new XSSFWorkbook();
                com.jdbaptista.app.labor.Formatter formatter = new Formatter(wb, outFolder);
                formatter.writeJob(job);
            }
        } catch(Exception e) {
            e.printStackTrace();
            outputLog.write("Something went wrong generating the reports.\n");
            outputLog.write("Check the input files.\n");
        }
        outputLog.write("Reports generated successfully.\n");
    }

    private void parseData() throws IOException {
        String nameCell;
        String addressCell;
        String dateCell;
        String taskCell;
        double timeCell;
        int classCell;
        String multiplierCell;

        // try to open the input file.
        Workbook wb;
        Sheet sheet = null;
        try {
            wb = WorkbookFactory.create(inFile);
            sheet = wb.getSheetAt(0);
        } catch (IOException e) {
            outputLog.write("Something went wrong reading the input.\n");
            e.printStackTrace();
        }

        // try to read the data.
        assert sheet != null;
        for (Row row : sheet) {
            Iterator<Cell> cellIterator = row.cellIterator();
            Cell cell;
            try {
                cell = cellIterator.next();
                nameCell = cell.toString().strip();
                cell = cellIterator.next();
                addressCell = cell.toString().strip();
                cell = cellIterator.next();
                dateCell = cell.toString().strip();
                cell = cellIterator.next();
                taskCell = cell.toString().strip();
                cell = cellIterator.next();
                timeCell = Double.parseDouble(cell.toString());
                cell = cellIterator.next();
                classCell = (int) Double.parseDouble(cell.toString());
                if (cellIterator.hasNext()) {
                    cell = cellIterator.next();
                    multiplierCell = cell.toString();
                } else {
                    multiplierCell = "";
                }
            } catch (Exception e) {
                outputLog.write("Something went wrong reading the daily tasks at row ");
                outputLog.write((row.getRowNum() + 1) + ". Skipped this row.\n");
                continue;
            }
            if (nameCell == null || addressCell == null || dateCell == null || taskCell == null || timeCell == -1 || classCell == -1) {
                outputLog.write("Row " + row.getRowNum() + " is not formatted correctly.");
                return;
            }

            // try to input the row.
            try {
                inputContainer(nameCell, addressCell, dateCell, taskCell, timeCell, classCell, multiplierCell);
            } catch (Exception e) {
                outputLog.write(e + "\n");
                return;
            }
        }
    }

    private void inputContainer(String name, String address, String date, String task, double time, int type, String multiplier) throws Exception {
        // parse the raw date into usable data.
        // raw date in form 01-Mar-2021.
        int day;
        String month;
        int year;
        LocalDate containerDate;

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        try {
            containerDate = LocalDate.parse(date, df);
            String[] splitDate = date.split("-");
            day = Integer.parseInt(splitDate[0]);
            month = splitDate[1];
            year = Integer.parseInt(splitDate[2]);
        } catch (Exception e) {
            throw new Exception("Date is not formatted correctly.");
        }

        // add new job to jobs list.
        Job newJob = new Job(address);
        Job jobRef = getJobRef(newJob);
        // This doubles for checking if newJob is in jobs hashmap.
        if (jobRef == null) {
            jobRef = newJob;
            jobs.put(jobRef, null);
        }

        // add new week to week list

        // add new month to the specified job's month list.
        Week newMonth = new Week(month, containerDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                                containerDate.get(ChronoField.ALIGNED_WEEK_OF_MONTH), year);
        Week monthRef = jobRef.getWeekRef(newMonth);
        if (monthRef == null) {
            monthRef = newMonth;
            jobRef.addWeek(monthRef);
        }


        // add new container to the specified month's container list.
        double doubleMultiplier;
        if (multiplier.equals("")) {
            doubleMultiplier = 1.0;
        } else {
            doubleMultiplier = Double.parseDouble(multiplier);
        }
        Container newContainer = new Container(day, containerDate, name, task, time, type, doubleMultiplier);
        Container containerRef = monthRef.getContainerRef(newContainer);
        if (containerRef == null) {
            containerRef = newContainer;
            monthRef.addContainer(containerRef);
        }
    }

    private Job getJobRef(Job query) {
        if (!jobs.containsKey(query)) return null;
        for (Job job : jobs.keySet()) {
            if (job.equals(query)) {
                return job;
            }
        }
        return null;
    }

    private void calculate(DatedTableData<Integer, Double> wcData) throws Exception {
        for (Job job : jobs.keySet()) {
            for (Week month : job.getWeeks()) {
                for (Container container : month.getContainers()) {
                    try {
                        container.amount = calculatePay(container, container.date);
                        container.wc = calculateWC(container, container.date, wcData);
                        container.tax = calculateTax(container);
                    } catch (Exception e) {
                        outputLog.write("It is probable that " + container.name);
                        outputLog.write(" is misspelled or missing from the salaries file.\n");
                        e.printStackTrace();
                        throw new Exception();
                    }
                }
                month.calculateDailyTotals();
                month.calculateTaskTotals();
            }
        }
    }

    private void addSalaryDate(String date, String[] names, String[] values) throws Exception {
        HashMap<String, Double> types = new HashMap<>();

        int day;
        String month;
        int intMonth;
        int year;
        try {
            String[] splitDate = date.split("-");
            day = Integer.parseInt(splitDate[0]);
            month = splitDate[1];
            year = Integer.parseInt(splitDate[2]);
        } catch (Exception e) {
            throw new Exception("Date is not formatted correctly.");
        }
        intMonth = convertMonth(month);

        for (int i = 0; i < names.length; i++) {
            if (values[i].equals("")) throw new Exception("");
            types.put(names[i], Double.parseDouble(values[i]));
        }
        salaries.put(LocalDate.of(year, intMonth, day), types);
    }

    private double calculateWC(Container container, LocalDate date, DatedTableData<Integer, Double> wcData) throws DatedTableException {
        double wcPercentage = wcData.getPercentage(container.type, date);
        return ((int) Math.round(container.amount * wcPercentage) / 100d);
    }

    private double calculatePay(Container container, LocalDate date) {
        LocalDate holdDate = null;
        ArrayList<LocalDate> dateChanges = new ArrayList<>(salaries.keySet());
        Collections.sort(dateChanges);
        for (LocalDate changeDate : dateChanges) {
            if (date.isAfter(changeDate))
                holdDate = changeDate;
            else
                break;
        }
        return ((int) Math.round(container.multiplier * container.time * salaries.get(holdDate).get(container.name) * 100)) / 100d;
    }

    private double calculateTax(Container container) {
        return ((int) Math.round(container.amount * 7.7)) / 100d;
    }

    public static int convertMonth(String month) {
        switch (month) {
            case "Jan" -> {
                return 1;
            }
            case "Feb" -> {
                return 2;
            }
            case "Mar" -> {
                return 3;
            }
            case "Apr" -> {
                return 4;
            }
            case "May" -> {
                return 5;
            }
            case "Jun" -> {
                return 6;
            }
            case "Jul" -> {
                return 7;
            }
            case "Aug" -> {
                return 8;
            }
            case "Sep" -> {
                return 9;
            }
            case "Oct" -> {
                return 10;
            }
            case "Nov" -> {
                return 11;
            }
            case "Dec" -> {
                return 12;
            }
            default -> {
                return -1;
            }
        }
    }

}

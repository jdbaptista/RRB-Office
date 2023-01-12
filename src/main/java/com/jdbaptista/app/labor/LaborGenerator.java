package com.jdbaptista.app.labor;

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
    }

    /**
     * Entry point for program execution and report generation.
     * @throws IOException
     */
    public void run() throws IOException {
        try {
            long startTime = System.nanoTime();
            System.out.println("Loading worker comp data...");
            DatedTableData<String, Double> wcData = DatedTableData.loadExcelData(workCompFile);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

            startTime = System.nanoTime();
            System.out.println("Loading salary data...");
            DatedTableData<String, Double> salaryData = DatedTableData.loadExcelData(salaryFile);
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
            calculate(wcData, salaryData);
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

            startTime = System.nanoTime();
            System.out.println("Generating reports...");
            generateFiles();
            endTime = System.nanoTime();
            duration = (endTime - startTime) / 1000000;
            System.out.println("Process finished in " + duration + "ms.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        outputLog.flush();
    }

    /**
     * Creates final labor reports in {@link LaborGenerator#outFolder}.
     * @throws IOException
     */
    private void generateFiles() throws IOException {
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

    /**
     * Loads shift data from {@link LaborGenerator#inFile}.
     * @throws IOException
     */
    private void parseData() throws IOException {
        String nameCell;
        String addressCell;
        String dateCell;
        String taskCell;
        double timeCell;
        String classCell;
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
                nameCell = cell.getStringCellValue().strip();
                cell = cellIterator.next();
                addressCell = cell.getStringCellValue().strip();
                cell = cellIterator.next();
                dateCell = cell.getStringCellValue().strip();
                cell = cellIterator.next();
                taskCell = cell.getStringCellValue().strip();
                cell = cellIterator.next();
                timeCell = cell.getNumericCellValue();
                cell = cellIterator.next();
                classCell = cell.getStringCellValue().strip();
                if (cellIterator.hasNext()) {
                    cell = cellIterator.next();
                    multiplierCell = cell.getStringCellValue().strip();
                } else {
                    multiplierCell = "";
                }
            } catch (Exception e) {
                outputLog.write("Something went wrong reading the daily tasks at row ");
                outputLog.write((row.getRowNum() + 1) + ". Skipped this row.\n");
                continue;
            }
            if (nameCell == null || addressCell == null || dateCell == null || taskCell == null || timeCell == -1 || classCell == null) {
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

    /**
     * Helper function of {@link LaborGenerator#parseData()}. Takes a row of data from {@link LaborGenerator#inFile}
     * and loads it into a {@link Container}.
     * @throws Exception
     */
    private void inputContainer(String name, String address, String date, String task, double time, String type, String multiplier) throws Exception {
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

    /**
     * Populates the {@link Container#tax}, and {@link Container#wc}. Tax percentage is hard coded in :P.
     * @param wcData Data used to calculate {@link Container#wc}.
     * @throws Exception
     */
    private void calculate(DatedTableData<String, Double> wcData, DatedTableData<String, Double> salaryData) throws Exception {
        for (Job job : jobs.keySet()) {
            for (Week month : job.getWeeks()) {
                for (Container cntr : month.getContainers()) {
                    try {
                        cntr.amount = salaryData.getValue(cntr.name, cntr.date) * cntr.time * cntr.multiplier;
                        cntr.wc = ((int) Math.round(cntr.amount * wcData.getValue(cntr.type, cntr.date)) / 100d);
                        //TODO: Unhardcode tax percentage.
                        cntr.tax = ((int) Math.round(cntr.amount * 7.7)) / 100d;
                    } catch (Exception e) {
                        outputLog.write("It is probable that " + cntr.name);
                        outputLog.write(" is misspelled or missing from the salaries file.\n");
                        e.printStackTrace();
                        throw e;
                    }
                }
                month.calculateDailyTotals();
                month.calculateTaskTotals();
            }
        }
    }
}

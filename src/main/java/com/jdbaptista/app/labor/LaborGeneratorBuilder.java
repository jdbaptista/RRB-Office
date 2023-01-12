package com.jdbaptista.app.labor;

import com.jdbaptista.app.labor.error.*;

import java.io.File;
import java.io.OutputStream;

public class LaborGeneratorBuilder {
    // required parameters
    private File inFile;
    private File workCompFile;
    private File salaryFile;
    private String outFolder;

    // optional parameters
    private OutputStream outputLog;

    public LaborGenerator build() throws LaborGeneratorException {
        // validate attributes
        this.inFile = validateInFile(inFile);
        this.workCompFile = validateWorkCompFile(workCompFile);
        this.salaryFile = validateSalaryFile(salaryFile);
        this.outputLog = validateOutputLog(outputLog);
        return new LaborGenerator(this);
    }

    protected File validateInFile(File inFile) throws LaborGeneratorException {
        return validateFile(inFile, "Dailies.xlsx");
    }

    protected File validateWorkCompFile(File workCompFile) throws LaborGeneratorException {
        return validateFile(workCompFile, "WCPercentages.xlsx");
    }

    protected File validateSalaryFile(File salaryFile) throws LaborGeneratorException {
        return validateFile(salaryFile, "Salaries.xlsx");
    }

    protected File validateFile(File file, String expectedName) throws LaborGeneratorException {
        if (!salaryFile.canRead())
            throw new LaborGeneratorException(file.getName() + " cannot be read!");
        if (!salaryFile.getName().equals(expectedName))
            throw new LaborGeneratorException(file.getAbsolutePath() + "is misnamed!");
        return file;
    }

    protected OutputStream validateOutputLog(OutputStream outputLog) {
        if (outputLog == null) return System.out;
        return outputLog;
    }

    public File getInFile() {
        return inFile;
    }

    public File getWorkCompFile() {
        return workCompFile;
    }

    public File getSalaryFile() {
        return salaryFile;
    }

    public String getOutFolder() {
        return outFolder;
    }

    public OutputStream getOutputLog() {
        return outputLog;
    }

    /**
     * Sets the input file containing daily employee shift data. Each row of the file should be formatted as:
     * (employee name as referred to in {@link LaborGeneratorBuilder#salaryFile}, job/client name, shift date,
     * task date, hours worked (integer or floating point), worker compensation code as referred to in
     * {@link LaborGeneratorBuilder#workCompFile}, optional: wage multiplier (defaults to 1.0 and is typically
     * used for overtime at a value of 1.5).
     * @param inFile
     * @return
     */
    public LaborGeneratorBuilder setInFile(File inFile) {
        this.inFile = inFile;
        return this;
    }

    /**
     * Sets file containing worker compensation data of {@link DatedTableData} form, where the column label type is
     * an integer code of the work type and the changing value type is a currency/floating point number describing
     * the percentage of wage taken by worker's compensation.
     */
    public LaborGeneratorBuilder setWorkCompFile(File workCompFile) {
        this.workCompFile = workCompFile;
        return this;
    }

    /**
     * Sets file containing salary data of {@link DatedTableData} form, where the column label type is employee names
     * as a string, and the changing value type is a currency/floating point number describing hourly wage.
     */
    public LaborGeneratorBuilder setSalaryFile(File salaryFile) {
        this.salaryFile = salaryFile;
        return this;
    }

    /**
     * Sets the folder location of where reports will be generated to.
     */
    public LaborGeneratorBuilder setOutFolder(String outFolder) {
        this.outFolder = outFolder;
        return this;
    }

    /**
     * An advanced, optional setting.
     * Sets the output stream where the user log messages will be written to.
     * Debug and error messages will always end up in the console.
     */
    public LaborGeneratorBuilder setOutputLog(OutputStream outputLog) {
        this.outputLog = outputLog;
        return this;
    }
}

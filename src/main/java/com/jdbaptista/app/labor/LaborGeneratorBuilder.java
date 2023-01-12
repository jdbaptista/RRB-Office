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

    public LaborGeneratorBuilder setInFile(File inFile) {
        this.inFile = inFile;
        return this;
    }

    public LaborGeneratorBuilder setWorkCompFile(File workCompFile) {
        this.workCompFile = workCompFile;
        return this;
    }

    public LaborGeneratorBuilder setSalaryFile(File salaryFile) {
        this.salaryFile = salaryFile;
        return this;
    }

    public LaborGeneratorBuilder setOutFolder(String outFolder) {
        this.outFolder = outFolder;
        return this;
    }

    public LaborGeneratorBuilder setOutputLog(OutputStream outputLog) {
        this.outputLog = outputLog;
        return this;
    }
}

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

    public LaborGenerator build() throws LaborGeneratorException {
        // validate attributes
        this.inFile = validateInFile(inFile);
        this.workCompFile = validateWorkCompFile(workCompFile);
        this.salaryFile = validateSalaryFile(salaryFile);
        this.outFolder = validateOutFolder(outFolder);
        this.outputLog = validateOutputLog(outputLog);

        return new LaborGenerator(this);
    }

    protected File validateInFile(File inFile) throws InFileException {
        if (!inFile.canRead())
            throw new InFileException(inFile.getName() + " cannot be read!");
        if (!inFile.getName().equals("Dailies.xlsx"))
            throw new InFileException(inFile.getAbsolutePath() + "is misnamed!");

        return inFile;
    }

    protected File validateWorkCompFile(File workCompFile) throws WorkCompFileException {
        if (!workCompFile.canRead())
            throw new WorkCompFileException(workCompFile.getName() + " cannot be read!");
        if (!workCompFile.getName().equals("WCPercentages.xlsx"))
            throw new WorkCompFileException(workCompFile.getAbsolutePath() + "is misnamed!");

        return workCompFile;
    }

    protected File validateSalaryFile(File salaryFile) throws SalaryFileException {
        if (!salaryFile.canRead())
            throw new SalaryFileException(salaryFile.getName() + " cannot be read!");
        if (!salaryFile.getName().equals("Salaries.xlsx"))
            throw new SalaryFileException(salaryFile.getAbsolutePath() + "is misnamed!");

        return salaryFile;
    }

    protected String validateOutFolder(String outFolder) {
        return outFolder;
    }

    protected OutputStream validateOutputLog(OutputStream outputLog) {
        if (outputLog == null) return System.out;
        return outputLog;
    }
}

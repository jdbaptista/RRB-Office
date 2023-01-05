package com.jdbaptista.app.material;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Formatter {
    private final HashMap<String, CellStyle> styles;
    private final ArrayList<String> allVendors;

    public Formatter(XSSFWorkbook wb) {
        styles = new HashMap<>();
        allVendors = new ArrayList<>();
        loadStyles(wb);
    }

    public void writeWeek(Sheet sheet, Container[] containers, String header) {
        int rowNum = 0;
        int cellNum = 0;

        // write header
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(cellNum);
        cell.setCellValue(header);
        cell.setCellStyle(styles.get("TITLE"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum, cellNum + 2));
        row = sheet.createRow(rowNum++);
        cell = row.createCell(cellNum++);
        cell.setCellValue("Client");
        cell.setCellStyle(styles.get("HEADER"));
        cell = row.createCell(cellNum++);
        cell.setCellValue("Vendor");
        cell.setCellStyle(styles.get("HEADER"));
        cell = row.createCell(cellNum++);
        cell.setCellValue("Date");
        cell.setCellStyle(styles.get("HEADER"));
        cell = row.createCell(cellNum++);
        cell.setCellValue("Amount");
        cell.setCellStyle(styles.get("HEADER"));
        rowNum++;
        cellNum = 0;

        // write each address to the sheet
        // use arraylist to keep order created by stream
        ArrayList<ArrayList<Container>> addresses = new ArrayList<>();
        for (Container c : containers) {
            // sort containers by address
            ArrayList<Container> addressRef = null;
            for (ArrayList<Container> address : addresses) {
                if (address.get(0).address().equals(c.address())) addressRef = address;
            }
            if (addressRef == null) {
                addressRef = new ArrayList<>();
                addresses.add(addressRef);
            }
            addressRef.add(c);

            // keep track of all vendors for later totals
            if (!allVendors.contains(c.vendor())) allVendors.add(c.vendor());
        }

        // initialize week vendor totals
        HashMap<String, Double> vendorTotals = new HashMap<>();
        for (String vendor : allVendors) {
            vendorTotals.put(vendor, 0.0);
        }

        for (ArrayList<Container> address : addresses) {
            ArrayList<Object> ret = writeAddress(sheet, address, address.get(0).address(), rowNum, cellNum);
            rowNum = (int) ret.get(0);
            cellNum = (int) ret.get(1);
            HashMap<String, Double> addressVendorTotals = (HashMap<String, Double>) ret.get(2);
            // add addressVendorTotals to weekTotal
            for (String vendor : addressVendorTotals.keySet()) {
                Double newTot = vendorTotals.get(vendor) + addressVendorTotals.get(vendor);
                vendorTotals.put(vendor, newTot);
            }

        }

        // write vendor and week totals
        row = sheet.createRow(rowNum++);
        cell = row.createCell(cellNum++);
        cell.setCellValue("VENDOR TOTALS");
        cell.setCellStyle(styles.get("VENDORHEADER"));
        row.createCell(cellNum++).setCellStyle(styles.get("VENDORHEADER"));
        row.createCell(cellNum++).setCellStyle(styles.get("VENDORHEADER"));
        row.createCell(cellNum++).setCellStyle(styles.get("VENDORHEADER"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum - 4, cellNum - 1));
        cellNum = 0;
        Double tot = 0.0;
        for (String vendor : vendorTotals.keySet()) {
            if (vendorTotals.get(vendor) == 0.0) continue;
            row = sheet.createRow(rowNum++);
            cell = row.createCell(cellNum++);
            cell.setCellValue(vendor);
            cell.setCellStyle(styles.get("VENDOR"));
            cell = row.createCell(cellNum++);
            cell.setCellStyle(styles.get("VENDOR"));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum - 2, cellNum));
            cell = row.createCell(cellNum++);
            cell.setCellStyle(styles.get("VENDOR"));
            cell = row.createCell(cellNum++);
            cell.setCellStyle(styles.get("VENDOR"));
            cell.setCellValue(vendorTotals.get(vendor));
            cell.setCellStyle(styles.get("AMOUNT"));
            tot += vendorTotals.get(vendor);
            cellNum = 0;
        }
        row = sheet.createRow(rowNum++);
        cell = row.createCell(cellNum++);
        cell.setCellValue("GRAND TOTAL");
        cell.setCellStyle(styles.get("GRANDTOTAL"));
        cell = row.createCell(cellNum++);
        cell.setCellStyle(styles.get("GRANDTOTAL"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum - 2, cellNum));
        cell = row.createCell(cellNum++);
        cell.setCellStyle(styles.get("AMOUNTTOTAL"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(tot);
        cell.setCellStyle(styles.get("AMOUNTTOTAL"));
        cellNum = 0;

        // auto size columns because sheet is not meant to be printed
        // and numbers can be quite large / weird
        for (int i = 0; i <= 5; i++) {
            sheet.autoSizeColumn(i);
        }

    }

    /*
        returns: {rowNum, cellNum, addressTotal}
     */
    private ArrayList<Object> writeAddress(Sheet sheet, ArrayList<Container> containers, String address, int rowNum, int cellNum) {
        double total = 0;

        // addressVendors for keeping track of vendor totals and such
        HashMap<String, Double> addressVendors = new HashMap<>();

        for (String vendor : allVendors) {
            addressVendors.put(vendor, 0.0);
        }

        // write containers
        Row row;
        Cell cell;
        cellNum = 0;
        String currVendor = null;
        for (int i = 0; i < containers.size(); i++) {
            if (i == containers.size() - 1) {
                if (currVendor == null || !currVendor.equals(containers.get(i).vendor())) {
                    // this container is the first and is the only of a vendor
                    // write container as vendor total
                    row = sheet.createRow(rowNum++);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).address());
                    cell.setCellStyle(styles.get("CLIENT"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor());
                    cell.setCellStyle(styles.get("VENDORTOTAL"));
                    cell = row.createCell(cellNum++);
                    LocalDate date = containers.get(i).date();
                    cell.setCellValue(date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
                    cell.setCellStyle(styles.get("DATE"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor());
                    cell.setCellStyle(styles.get("VENDORTOTAL"));
                    cell.setCellValue(containers.get(i).amount());
                    cell.setCellStyle(styles.get("AMOUNTTOTAL"));
                    Double newTot = containers.get(i).amount() + addressVendors.get(containers.get(i).vendor());
                    addressVendors.put(containers.get(i).vendor(), newTot);
                    currVendor = containers.get(i).vendor();
                } else {
                    // write container
                    row = sheet.createRow(rowNum++);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).address());
                    cell.setCellStyle(styles.get("CLIENT"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor());
                    cell.setCellStyle(styles.get("VENDOR"));
                    cell = row.createCell(cellNum++);
                    LocalDate date = containers.get(i).date();
                    cell.setCellValue(date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
                    cell.setCellStyle(styles.get("DATE"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).amount());
                    cell.setCellStyle(styles.get("AMOUNT"));
                    Double newTot = containers.get(i).amount() + addressVendors.get(containers.get(i).vendor());
                    addressVendors.put(containers.get(i).vendor(), newTot);
                    cellNum = 0;

                    // write vendor total
                    row = sheet.createRow(rowNum++);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).address());
                    cell.setCellStyle(styles.get("CLIENT"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor() + " Total");
                    cell.setCellStyle(styles.get("VENDORTOTAL"));
                    cell = row.createCell(cellNum++);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum - 2, cellNum - 1));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(addressVendors.get(containers.get(i).vendor()));
                    cell.setCellStyle(styles.get("AMOUNTTOTAL"));
                }
            } else if (!containers.get(i).vendor().equals(containers.get(i + 1).vendor())) {
                if (currVendor == null) {
                    // this container is the first and only of a vendor
                    row = sheet.createRow(rowNum++);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).address());
                    cell.setCellStyle(styles.get("CLIENT"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor());
                    cell.setCellStyle(styles.get("VENDORTOTAL"));
                    cell = row.createCell(cellNum++);
                    LocalDate date = containers.get(i).date();
                    cell.setCellValue(date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
                    cell.setCellStyle(styles.get("DATE"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor());
                    cell.setCellStyle(styles.get("VENDORTOTAL"));
                    cell.setCellValue(containers.get(i).amount());
                    cell.setCellStyle(styles.get("AMOUNTTOTAL"));
                    Double newTot = containers.get(i).amount() + addressVendors.get(containers.get(i).vendor());
                    addressVendors.put(containers.get(i).vendor(), newTot);
                    currVendor = containers.get(i).vendor();
                } else if (!currVendor.equals(containers.get(i).vendor())) {
                    // this is the first of a new vendor
                    row = sheet.createRow(rowNum++);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).address());
                    cell.setCellStyle(styles.get("CLIENT"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor());
                    cell.setCellStyle(styles.get("VENDOR"));
                    cell = row.createCell(cellNum++);
                    LocalDate date = containers.get(i).date();
                    cell.setCellValue(date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
                    cell.setCellStyle(styles.get("DATE"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).amount());
                    cell.setCellStyle(styles.get("AMOUNT"));
                    Double newTot = containers.get(i).amount() + addressVendors.get(containers.get(i).vendor());
                    addressVendors.put(containers.get(i).vendor(), newTot);
                    currVendor = containers.get(i).vendor();
                } else {
                    // write container
                    row = sheet.createRow(rowNum++);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).address());
                    cell.setCellStyle(styles.get("CLIENT"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).vendor());
                    cell.setCellStyle(styles.get("VENDOR"));
                    cell = row.createCell(cellNum++);
                    LocalDate date = containers.get(i).date();
                    cell.setCellValue(date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
                    cell.setCellStyle(styles.get("DATE"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i).amount());
                    cell.setCellStyle(styles.get("AMOUNT"));
                    Double newTot = containers.get(i).amount() + addressVendors.get(containers.get(i).vendor());
                    addressVendors.put(containers.get(i).vendor(), newTot);
                    cellNum = 0;

                    // write previous vendor total
                    row = sheet.createRow(rowNum++);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i - 1).address());
                    cell.setCellStyle(styles.get("CLIENT"));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(containers.get(i - 1).vendor() + " Total");
                    cell.setCellStyle(styles.get("VENDORTOTAL"));
                    cell = row.createCell(cellNum++);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum - 2, cellNum - 1));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(addressVendors.get(containers.get(i - 1).vendor()));
                    cell.setCellStyle(styles.get("AMOUNTTOTAL"));
                    currVendor = containers.get(i).vendor();
                }
            } else {
                // write container
                row = sheet.createRow(rowNum++);
                cell = row.createCell(cellNum++);
                cell.setCellValue(containers.get(i).address());
                cell.setCellStyle(styles.get("CLIENT"));
                cell = row.createCell(cellNum++);
                cell.setCellValue(containers.get(i).vendor());
                cell.setCellStyle(styles.get("VENDOR"));
                cell = row.createCell(cellNum++);
                LocalDate date = containers.get(i).date();
                cell.setCellValue(date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
                cell.setCellStyle(styles.get("DATE"));
                cell = row.createCell(cellNum++);
                cell.setCellValue(containers.get(i).amount());
                cell.setCellStyle(styles.get("AMOUNT"));
                Double newTot = containers.get(i).amount() + addressVendors.get(containers.get(i).vendor());
                addressVendors.put(containers.get(i).vendor(), newTot);
                currVendor = containers.get(i).vendor();
            }
            total += containers.get(i).amount();
            cellNum = 0;
        }

        // write address total
        row = sheet.createRow(rowNum++);
        cell = row.createCell(cellNum++);
        cell.setCellValue(containers.get(0).address());
        cell.setCellStyle(styles.get("CLIENT"));
        cell = row.createCell(cellNum++);
        cell.setCellStyle(styles.get("GRANDTOTAL"));
        cell.setCellValue("Grand Total");
        cell = row.createCell(cellNum++);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum - 2, cellNum - 1));
        cell.setCellStyle(styles.get("GRANDTOTAL"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(total);
        cell.setCellStyle(styles.get("AMOUNTTOTAL"));
        sheet.createRow(rowNum++);
        cellNum = 0;

        // return variables
        ArrayList<Object> ret = new ArrayList<Object>();
        ret.add(rowNum);
        ret.add(cellNum);
        ret.add(addressVendors);

        return ret;
    }

    /**
     * Loads all styles to a workbook.
     * @since 1.1
     */
    public void loadStyles(XSSFWorkbook wb) {
        styles.put("TITLE", createTitleFont(wb));
        styles.put("HEADER", createHeaderFont(wb));
        styles.put("VENDORHEADER", createVendorHeaderStyle(wb));
        styles.put("CLIENT", createClientFont(wb));
        styles.put("VENDOR", createVendorFont(wb));
        styles.put("DATE", createDateFont(wb));
        styles.put("AMOUNT", createAmountFont(wb));
        styles.put("VENDORTOTAL", createVendorTotalFont(wb));
        styles.put("DATETOTAL", createDateTotalFont(wb));
        styles.put("AMOUNTTOTAL", createAmountTotalFont(wb));
        styles.put("GRANDTOTAL", createGrandTotalFont(wb));
    }


    private CellStyle createTitleFont(Workbook wb) {
        // set up the style
        CellStyle STYLE = wb.createCellStyle();
        // modify the style
        STYLE.setAlignment(HorizontalAlignment.CENTER);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        return STYLE;
    }

    private CellStyle createHeaderFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        // modify the font
        FONT.setBold(true);
        return STYLE;
    }

    private CellStyle createVendorHeaderStyle(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        STYLE.setAlignment(HorizontalAlignment.CENTER);
        // modify the font
        FONT.setBold(true);
        return STYLE;
    }

    private CellStyle createClientFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        // modify the font
        FONT.setBold(true);
        return STYLE;
    }

    private CellStyle createVendorFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        return STYLE;
    }

    private CellStyle createDateFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        return STYLE;
    }

    private CellStyle createVendorTotalFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        // modify the font
        FONT.setBold(true);
        return STYLE;
    }

    private CellStyle createDateTotalFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        // modify the font
        FONT.setBold(true);
        return STYLE;
    }

    private CellStyle createAmountFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        STYLE.setAlignment(HorizontalAlignment.RIGHT);
        STYLE.setDataFormat((short) 8); // currency format
        return STYLE;
    }

    private CellStyle createAmountTotalFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        STYLE.setAlignment(HorizontalAlignment.RIGHT);
        STYLE.setDataFormat((short) 8); // currency format
        // modify the font
        FONT.setBold(true);
        return STYLE;
    }

    private CellStyle createGrandTotalFont(Workbook wb) {
        // set up the style and font and link together
        CellStyle STYLE = wb.createCellStyle();
        Font FONT = wb.createFont();
        STYLE.setFont(FONT);
        // modify the style
        STYLE.setBorderTop(BorderStyle.THIN);
        STYLE.setBorderRight(BorderStyle.THIN);
        STYLE.setBorderBottom(BorderStyle.THIN);
        STYLE.setBorderLeft(BorderStyle.THIN);
        STYLE.setVerticalAlignment(VerticalAlignment.CENTER);
        STYLE.setAlignment(HorizontalAlignment.CENTER);
        // modify the font
        FONT.setBold(true);
        FONT.setItalic(true);
        return STYLE;
    }

}

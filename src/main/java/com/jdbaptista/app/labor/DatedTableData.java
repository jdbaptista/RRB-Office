package com.jdbaptista.app.labor;

import com.jdbaptista.app.labor.error.DatedTableException;

import java.time.LocalDate;
import java.util.HashMap;

public class DatedTableData<T, S> {
    /**
     * Main data structure of this class, which everything is an abstraction of.
     */
    private final HashMap<T, DatedTableNode<S>> codeToNodes = new HashMap<>();

    /**
     * Meant to hold the column number of all codes for easy lookup when inserting
     * percentages into the WCData.
     */
    public HashMap<Integer, T> colNumToCode = new HashMap<>();

    public void addCode(T code, int columnNum) {
        codeToNodes.put(code, null);
        colNumToCode.put(columnNum, code);
    }

    /**
     * Appends a new node which will continue into the present, effectively adding an
     * end date to the node currently referring to the present.
     */
    public void addChangeByCode(T targetCode, S percentage, LocalDate startDate) throws DatedTableException {
        if (!codeToNodes.containsKey(targetCode)) {
            throw new DatedTableException("Code " + targetCode + " does not exist.");
        }
        // note: dummy head node will have proper endDate
        DatedTableNode<S> newNode = new DatedTableNode<>(percentage, startDate);
        DatedTableNode<S> prevNode = getLast(targetCode);

        if (!startDate.isAfter(prevNode.getStartDate())) {
            throw new DatedTableException("Dates are misordered.");
        }

        prevNode.setNext(newNode);
    }

    public void addChangeByCol(int columnNum, S percentage, LocalDate startDate) throws DatedTableException {
        if (!colNumToCode.containsKey(columnNum)) {
            throw new DatedTableException("Column " + columnNum + " does not contain a code.");
        }

        // note: dummy head node will have proper endDate
        T code = colNumToCode.get(columnNum);
        DatedTableNode<S> newNode = new DatedTableNode<>(percentage, startDate);
        DatedTableNode<S> prevNode = getLast(code);

        if (prevNode == null) {
            codeToNodes.put(code, newNode);
            return;
        }

        if (!startDate.isAfter(prevNode.getStartDate())) {
            throw new DatedTableException("Dates are misordered.");
        }

        prevNode.setNext(newNode);
    }

    /**
     *
     * @param targetCode the code associated to the shift's type of work.
     * @return The node referring to a range continuing into the present.
     */
    public DatedTableNode<S> getLast(T targetCode) throws DatedTableException {
        if (!codeToNodes.containsKey(targetCode)) throw new DatedTableException("Code does not exist.");
        if (codeToNodes.get(targetCode) == null) return null;

        DatedTableNode<S> curr = codeToNodes.get(targetCode);
        while (curr.hasNext()) {
            curr = curr.getNext();
        }
        return curr;
    }

    /**
     *
     * @param code The target worker compensation code. Likely a shift code.
     * @param date The target date. Likely a shift date.
     * @return The percentage of wage that shall be paid in worker compensation
     *  for the given code and date.
     */
    public S getPercentage(T code, LocalDate date) throws DatedTableException {
        DatedTableNode<S> curr = codeToNodes.get(code); // initially is a dummy node, makes life easier
        do {
            curr = curr.getNext();
            boolean currIsStart = date.isEqual(curr.getStartDate());
            boolean currInRange = date.isAfter(curr.getStartDate());
            if (curr.getEndDate() != null) currInRange = currInRange && date.isBefore(curr.getEndDate()); // fixes null
            if (currIsStart || currInRange) return curr.getPercentage();
        } while (curr.hasNext());

        // from here on, the date is either before applicable date range or between last node and present.
        DatedTableNode<S> head = codeToNodes.get(code);
        if (date.isBefore(head.getStartDate())) {
            throw new DatedTableException("Date " + date + " is undefined.");
        }

        return curr.getPercentage();
    }
}

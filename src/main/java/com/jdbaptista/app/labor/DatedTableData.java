package com.jdbaptista.app.labor;

import com.jdbaptista.app.labor.error.DatedTableException;

import java.time.LocalDate;
import java.util.HashMap;

/**
 * Represents an Excel table in which the rows are {@link LocalDate} in sequential order with last being the most
 * present, columns are labels (ex. employee names), and data is variable over time and corresponds to the column it
 * is under. Convention here is that the {@link LocalDate} of a row is that row's start date, and the previous row's
 * end date.
 * @param <T> Column label type.
 * @param <S> Changing value type.
 */
public class DatedTableData<T, S> {
    /**
     * Main data structure of this class. A hashmap is used to easily access data by column label. Internal data
     * is stored as a linked list of {@link DatedTableNode} for each column in sequential order with last being the
     * most present.
     */
    private final HashMap<T, DatedTableNode<S>> columnToNodes = new HashMap<>();

    /**
     * Serves as a quality of life structure to allow data lookup by column number instead of label.
     */
    private final HashMap<Integer, T> colNumToLabel = new HashMap<>();

    /**
     * Stores last node of a column that refers to the range currently referring to the present.
     * Useful in reducing running time of range additions.
     */
    private final HashMap<T, DatedTableNode<S>> columnToLastNode = new HashMap<>();

    public void addColumn(T label, int columnNum) throws DatedTableException {
        if (colNumToLabel.containsKey(columnNum)) {
            throw new DatedTableException("An attempt to add a duplicate column number has occurred.");
        }
        columnToNodes.put(label, null);
        colNumToLabel.put(columnNum, label);
    }

    /**
     * Appends a new range that will continue into the present, simultaneously
     * adding an end date to the range currently referring to the present. Implicit creation
     * of columns is not supported and must be done first via {@link DatedTableData#addColumn(Object, int)}.
     * @param label The label of the column this range will be added to.
     * @param newValue The new value this column will have between the startDate and present.
     * @param startDate The inclusive start of this range, and exclusive end of the previous range.
     * @throws DatedTableException
     */
    public void addRangeByLabel(T label, S newValue, LocalDate startDate) throws DatedTableException {
        if (!columnToNodes.containsKey(label)) {
            throw new DatedTableException("Column of " + label + " does not exist.");
        }
        DatedTableNode<S> prevNode = getLast(label);
        DatedTableNode<S> newNode = new DatedTableNode<>(newValue, startDate);
        if (prevNode == null) {
            columnToNodes.put(label, newNode);
        } else {
            if (!startDate.isAfter(prevNode.getStartDate())) {
                throw new DatedTableException("Dates are misordered.");
            }
            prevNode.setNext(newNode);
            columnToLastNode.put(label, newNode);
        }
    }

    /**
     * Appends a new range that will continue into the present, simultaneously
     * adding an end date to the range currently referring to the present. Implicit creation
     * of columns is not supported and must be done first via {@link DatedTableData#addColumn(Object, int)}.
     * @param columnNum The column number of the column this range will be added to.
     * @param newValue The new value this column will have between the startDate and present.
     * @param startDate The inclusive start of this range, and exclusive end of the previous range.
     * @throws DatedTableException
     */
    public void addRangeByColNum(int columnNum, S newValue, LocalDate startDate) throws DatedTableException {
        if (!colNumToLabel.containsKey(columnNum)) {
            throw new DatedTableException("Column " + columnNum + " does not contain a label.");
        }
        T label = colNumToLabel.get(columnNum);
        addRangeByLabel(label, newValue, startDate);
    }

    /**
     *
     * @param label The column label of target range.
     * @return The node referring to a range continuing into the present.
     */
    private DatedTableNode<S> getLast(T label) throws DatedTableException {
        if (!columnToNodes.containsKey(label)) {
            throw new DatedTableException("Column of " + label + " does not exist.");
        }
        return columnToLastNode.get(label);
    }

    /**
     *
     * @param label The target column label.
     * @param date The target date.
     * @return The value of the column at the date specified.
     */
    public S getValue(T label, LocalDate date) throws DatedTableException {
        DatedTableNode<S> curr = columnToNodes.get(label); // initially is a dummy node
        do {
            curr = curr.getNext();
            boolean currIsStart = date.isEqual(curr.getStartDate());
            boolean currInRange = date.isAfter(curr.getStartDate());
            if (curr.getEndDate() != null) currInRange = currInRange && date.isBefore(curr.getEndDate()); // fixes null
            if (currIsStart || currInRange) return curr.getValue();
        } while (curr.hasNext());
        // from here on, the date is either before applicable date range or between last node and present.
        DatedTableNode<S> head = columnToNodes.get(label);
        if (date.isBefore(head.getStartDate())) {
            throw new DatedTableException("Date " + date + " is undefined.");
        }
        return curr.getValue();
    }
}

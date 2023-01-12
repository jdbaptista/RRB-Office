package com.jdbaptista.app.labor;

import java.time.LocalDate;

/**
 * A linked-list node that holds information about one row within a {@link DatedTableData} instance.
 */
public class DatedTableNode<T> {

    /**
     * The percentage of worker wage that is used to
     * calculate amount of money owed to worker compensation.
     */
    final private T value;

    /**
     * The date when this node's percentage takes effect.
     */
    final private LocalDate startDate;

    /**
     * The successor to this node, which describes the end date of this
     * node's range. Null value indicates this node's percentage is in
     * effect up to the present.
     */
    private DatedTableNode<T> next;

    public DatedTableNode(T percentage, LocalDate startDate) {
        this.value = percentage;
        this.startDate = startDate;
    }

    public T getValue() {
        return value;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Gets the start date of the next node, which is also the
     * end date of this node. Here to reduce boilerplate.
     */
    public LocalDate getEndDate() {
        if (hasNext()) return next.getStartDate();
        return null;
    }

    public DatedTableNode<T> getNext() {
        return next;
    }

    public void setNext(DatedTableNode<T> next) {
        this.next = next;
    }

    public boolean hasNext() {
        return next != null;
    }
}

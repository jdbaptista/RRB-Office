package com.jdbaptista.app.labor;

import java.time.LocalDate;

/**
 * A linked-list node that holds information about worker compensation at
 * a given date range. This node is part of the WCData class which is simply
 * an abstraction of the following: HashMap where key: code & val: WCNode.
 */
public class DatedTableNode<T> {

    /**
     * The percentage of worker wage that is used to
     * calculate amount of money owed to worker compensation.
     */
    final private T percentage;

    /**
     * The date when this node's percentage takes effect.
     */
    final private LocalDate startDate;

    /**
     * The successor to this node, which describes the end date of this
     * node's range. null value indicates this node's percentage is in
     * effect up to the present.
     */
    private DatedTableNode<T> next;

    public DatedTableNode(T percentage, LocalDate startDate) {
        this.percentage = percentage;
        this.startDate = startDate;
    }

    public T getPercentage() {
        return percentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Simply gets the start date of the next node, which is also the
     * end date of this node. Here to reduce boilerplate.
     */
    public LocalDate getEndDate() {
        if (next == null) return null;
        return next.getStartDate();
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

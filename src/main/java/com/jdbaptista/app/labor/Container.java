package com.jdbaptista.app.labor;

import java.time.LocalDate;
import java.util.Objects;

public class Container implements Comparable<Container> {
    final public int day;
    final public LocalDate date;
    final public String name;
    final public String task;
    final public double time;
    final public String type;
    final public double multiplier;
    public double amount;
    public double wc;
    public double tax;


    public Container(int day, LocalDate date, String name, String task, double time, String type, double multiplier) {
        this.day = day;
        this.date = date;
        this.name = name;
        this.task = task;
        this.time = time;
        this.type = type;
        this.multiplier = multiplier;
        this.amount = -1;
    }

    public double getTotal() {
        return ((int) (amount) * 100) / 100d;
    }

    @Override
    public String toString() {
        return name + ", " + day;
    }

    @Override
    public boolean equals(Object query) {
        if (this == query) return true;
        if (query == null || getClass() != query.getClass()) return false;
        Container container = (Container) query;
        return day == container.day && Double.compare(container.time, time) == 0 && type == container.type && name.equals(container.name) && task.equals(container.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, name, task, time, type);
    }

    @Override
    public int compareTo(Container o) {
        return date.compareTo(o.date);
    }
}

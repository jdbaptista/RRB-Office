package com.jdbaptista.app.labor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents one client or address and all the data associated to it, mainly every {@link Week} for this job.
 */
public class Job {
    final private String address;
    final private ArrayList<Week> weeks;
    public HashMap<String, double[]> taskTotals;

    public Job(String address) {
        this.address = address;
        weeks = new ArrayList<>();
        taskTotals = new HashMap<>();
    }

    public void addWeek(Week week) {
        weeks.add(week);
    }

    public ArrayList<Week> getWeeks() {
        return weeks;
    }

    public Week getWeekRef(Week query) {
        if (!weeks.contains(query)) return null;
        for (Week week : weeks) {
            if (week.equals(query)) {
                return week;
            }
        }
        return null;
    }

    public double[] calculateJobTotal() {
        double[] jobTotals = new double[4];
        for (Week week : weeks) {
            double[] weekTotals = week.dailyTotals.get(0); // 0 is reserved for total
            for (int i = 0; i < weekTotals.length; i++) {
                jobTotals[i] += weekTotals[i];
            }
        }
        return jobTotals;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return job.getAddress().equals(address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}

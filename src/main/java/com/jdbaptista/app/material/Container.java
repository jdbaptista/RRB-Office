package com.jdbaptista.app.material;

import java.time.LocalDate;

public record Container(String address,
                        String vendor,
                        LocalDate date,
                        double amount) {

    @Override
    public String toString() {
        return ("Container[" + address + ":" + vendor + ":" + date + ":" + amount + "]");
    }
}

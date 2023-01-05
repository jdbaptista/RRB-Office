package com.jdbaptista.app.labor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.HashMap;


import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class LaborLoaderTest {

//    @Test
//    void loadWorkCompData() throws Exception {
//        File input1 = new File("src/test/files/LaborLoader/loadWorkCompData/input1.xlsx");
//        File output1 = new File("src/test/files/LaborLoader/loadWorkCompData/output1.json");
//        System.out.println(output1.toString());
//        Gson gson = new Gson();
//
////        Object actualOutput = loader.loadWorkCompData(input1, null);
//        Type type = new TypeToken<HashMap<LocalDate, HashMap<Integer, Double>>>(){}.getType();
//        HashMap<LocalDate, HashMap<Integer, Double>> testOutput = gson.fromJson(output1.toString(), type);
//    }

    @Test
    void convertMonth() {
    }
}
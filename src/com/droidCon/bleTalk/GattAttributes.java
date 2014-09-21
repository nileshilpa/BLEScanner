package com.droidCon.bleTalk;

import java.util.HashMap;

public class GattAttributes {

    private static HashMap<String, String> attributes = new HashMap();

    static {
        // Sample Services.
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb","Generic Access Service");

        //DROID CON SERVICE
        attributes.put("22222222-2222-2222-2222-222222222222","DroidCon Service One");
        attributes.put("ab8a0bca-2d06-4a40-ae3b-39136a9b04e9","DroidCon Service Two");


        // Sample Characteristics.
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("55555555-5555-5555-5555-555555555555","Service1-Char-1");

        //DROID CON Characteristics
        attributes.put("13d81d49-a8f6-4a3b-904d-61a17e8cbe6e","Service2-Char-1");
        attributes.put("c8c1f515-b8c5-4b52-a2b9-3de57fa6cc3d","Service2-Char-2");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

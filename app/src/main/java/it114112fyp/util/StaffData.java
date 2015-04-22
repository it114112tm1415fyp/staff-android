package it114112fyp.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class StaffData {
    public static DataList regionList = new DataList();
    public static ArrayList<JSONObject> todayTask = new ArrayList<>();
    public static ArrayList<JSONObject> shopList = new ArrayList<>();
    public static JSONObject staffObject = new JSONObject();
    public static JSONArray addresses;
    public static String username, password, name, email, phone, workplace, register_time, last_modify_time;
    public static int id ,conveyorId = 0;
    public static int SERVE_TASK_RECEIVE, SERVE_TASK_ISSUE;

    public static void resetData() {
        regionList = new DataList();
        todayTask = new ArrayList<>();
        shopList = new ArrayList<>();
        staffObject = new JSONObject();
        addresses = new JSONArray();
        username = "";
        password = "";
        workplace = "";
        name = "";
        email = "";
        phone = "";
        register_time = "";
        last_modify_time = "";
        id = 0;
        conveyorId = 0;
    }
}

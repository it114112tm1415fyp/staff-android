package it114112fyp.staff_android;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it114112fyp.util.AddressItem;
import it114112fyp.util.StaffData;

public final class HTTPRequest {

    public final static HashMap<String, String> cookie = new HashMap<String, String>();

    private final static String ServerUrl = "http://it114112tm1415fyp1.redirectme.net:6083/";

    public final static JSONObject request(String postposition, HashMap<String, String> parameters) {
        return request(postposition, parameters, "");
    }

    public final static JSONObject request(String postposition, HashMap<String, String> parameters, String formatedParameters) {
        return request(postposition, parameters, formatedParameters, true);
    }

    public final static JSONObject request(String postposition, HashMap<String, String> parameters, boolean post) {
        return request(postposition, parameters, "", post);
    }

    public final static JSONObject request(String postposition, HashMap<String, String> parameters, String formatedParameters, boolean post) {
        URL url = null;
        String formatedParameter = "";
        try {
            if (parameters != null && parameters.size() != 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (HashMap.Entry<String, String> entry : parameters.entrySet()) {
                    stringBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8")).append('=');
                    stringBuilder.append(URLEncoder.encode(entry.getValue(), "utf-8")).append('&');
                }
                stringBuilder.append(formatedParameters);
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                formatedParameter = stringBuilder.toString();
            }
            url = new URL(ServerUrl + postposition + (!post && formatedParameter.length() > 0 ? "?" + formatedParameter : ""));
            Log.d("URL", url.toString());
            Log.d("Parameters", formatedParameter);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if (cookie.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (HashMap.Entry<String, String> entry : cookie.entrySet()) {
                    stringBuilder.append(entry.getKey()).append('=');
                    stringBuilder.append(entry.getValue()).append("; ");
                }
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                httpURLConnection.setRequestProperty("cookie", stringBuilder.toString());
                Log.d("cookie", stringBuilder.toString());
            } else {
                Log.d("cookie", "no cookie");
            }
            if (post) {
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.connect();
                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.writeBytes(formatedParameter);
            }
            List<String> header_set_cookie = httpURLConnection.getHeaderFields().get("Set-Cookie");
            if (header_set_cookie != null) {
                for (String x : header_set_cookie) {
                    System.out.println(x);
                    String key = x.substring(0, x.indexOf("="));
                    String value = x.substring(x.indexOf("=") + 1, x.indexOf(";"));
                    cookie.put(key, value);
                }
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String result = bufferedReader.readLine();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result += '\n' + line;
            }
            bufferedReader.close();
            Log.d("result", result);
            JSONObject resultObject = new JSONObject(result);
            if (!checkResult(resultObject))
                return request(postposition, parameters, formatedParameters, post);
            else
                return resultObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Boolean checkResult(JSONObject result) throws JSONException {
        if (result.has("success")) {
            if (!result.getBoolean("success")) {
                if (result.getString("error").equals("Connection expired")) {
                    if (StaffData.username != null && !StaffData.username.equals("") && StaffData.password != null && !StaffData.password.equals("")) {
                        staff_login();
                        return false;
                    }
                }
                if (result.getString("error").equals("Login require")) {
                    if (StaffData.username != null && !StaffData.username.equals("") && StaffData.password != null && !StaffData.password.equals("")) {
                        staff_login();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static String md5(String s) {
        try {
            final char md5Chars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(s.getBytes());
            byte[] bytes = messageDigest.digest();
            int length = messageDigest.digest().length;
            StringBuffer stringbuffer = new StringBuffer(2 * length);
            for (int t = 0; t < length; t++) {
                stringbuffer.append(md5Chars[(bytes[t] & 0xf0) >> 4]);
                stringbuffer.append(md5Chars[bytes[t] & 0xf]);
            }
            return stringbuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static JSONObject staff_login() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", StaffData.username);
        parameters.put("password", md5(md5(StaffData.password)));
        return request("account/staff_login", parameters);
    }

    public static JSONObject logout() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        return request("account/logout", parameters);
    }

    public static JSONObject account_edit_profile(String password, String newPassword, String realName, String phone, String email,
                                                  ArrayList<AddressItem> addressList) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("password", md5(md5(password)));
        if (!newPassword.equals(""))
            parameters.put("new_password", md5(md5(newPassword)));
        parameters.put("name", realName);
        parameters.put("email", email);
        parameters.put("phone", phone);
        StringBuilder formatedParameter = new StringBuilder();
        for (int i = 0; i < addressList.size(); i++) {
            try {
                formatedParameter.append(URLEncoder.encode("addresses[][address]", "utf-8"));
                formatedParameter.append("=");
                formatedParameter.append(URLEncoder.encode(addressList.get(i).shortAddress, "utf-8"));
                formatedParameter.append("&");
                formatedParameter.append(URLEncoder.encode("addresses[][region_id]", "utf-8"));
                formatedParameter.append("=");
                formatedParameter.append(URLEncoder.encode(addressList.get(i).regionId + "", "utf-8"));
                formatedParameter.append("&");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request("account/edit_profile", parameters, formatedParameter.toString());
    }

    public static JSONObject conveyor_getList() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        return request("conveyor/get_list", parameters);
    }

    public static JSONObject get_control(int conveyor_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("conveyor_id", "" + conveyor_id);
        return request("conveyor/get_control", parameters);
    }

    public static JSONObject send_message(int conveyor_id, String message) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("conveyor_id", "" + conveyor_id);
        parameters.put("message", message);
        return request("conveyor/send_message", parameters);
    }

    public static JSONObject location_get_list() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        return request("location/get_list", parameters);
    }

    public static JSONObject region_get_list() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        return request("region/get_list", parameters);
    }

    public static JSONObject order_get_details(int order_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("order_id", "" + order_id);
        return request("order/get_details", parameters);
    }

    public static JSONObject location_get_specify_address_id(String address, int region_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("address", address);
        parameters.put("region_id", "" + region_id);
        return request("location/get_specify_address_id", parameters);
    }

    public static JSONObject order_edit(int order_id, int departure_id, String departure_type, int destination_id, String destination_type, int goods_number) {
        Log.d("http order edit", "for non-register user");
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("order_id", "" + order_id);
        parameters.put("departure_id", "" + departure_id);
        parameters.put("departure_type", "" + departure_type);
        parameters.put("destination_id", "" + destination_id);
        parameters.put("destination_type", "" + destination_type);
        parameters.put("goods_number", "" + goods_number);
        return request("order/edit", parameters);
    }

    public static JSONObject order_confirm(int task_id, int order_id, String sender_sign) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("task_id", task_id + "");
        parameters.put("order_id", "" + order_id);
        parameters.put("sign", sender_sign);
        return request("order/confirm", parameters);
    }

    public static JSONObject goods_get_details(String goodId) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("goods_id", goodId);
        return request("goods/get_details", parameters);
    }

    public static JSONObject goods_add(int task_id, int order_id, String good_id, float weight, boolean fragile, boolean flammable, String goods_photo) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("task_id", "" + task_id);
        parameters.put("goods_id", "" + good_id);
        parameters.put("order_id", "" + order_id);
        parameters.put("weight", "" + weight);
        parameters.put("fragile", "" + fragile);
        parameters.put("flammable", "" + flammable);
        parameters.put("goods_photo", goods_photo);
        return request("goods/add", parameters);
    }

    public static JSONObject goods_edit(int order_id, String good_id, float weight, boolean fragile, boolean flammable, String goods_photo) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("goods_id", "" + good_id);
        parameters.put("order_id", "" + order_id);
        parameters.put("weight", "" + weight);
        parameters.put("fragile", "" + fragile);
        parameters.put("flammable", "" + flammable);
        parameters.put("goods_photo", goods_photo);
        return request("goods/edit", parameters);
    }

    public static JSONObject good_remove(String good_id, int order_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("good_id", "" + good_id);
        parameters.put("order_id", "" + order_id);
        return request("good/remove", parameters);
    }

    public static JSONObject picture_good(String goods_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("goods_id", goods_id);
        return request("picture/goods", parameters);
    }

    public static JSONObject task_get_today_task() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        return request("task/get_today_tasks", parameters);
    }

    public static JSONObject task_get_details(int task_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("task_id", "" + task_id);
        return request("task/get_details", parameters);
    }

    public static JSONObject get_goods_qrcode(String goods_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("goods_id", goods_id);
        return request("goods/get_qr_code", parameters);
    }

    public static JSONObject task_contact(int task_id, int order_id, boolean customer_free) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("task_id", task_id + "");
        parameters.put("order_id", order_id + "");
        parameters.put("customer_free", customer_free + "");
        return request("task/contact", parameters);
    }

    public static JSONObject do_task(int task_id, int shelf_id, String goods_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("task_id", task_id + "");
        parameters.put("goods_id", goods_id);
//        StringBuilder formatedParameter = new StringBuilder();
//            try {
//                formatedParameter.append(URLEncoder.encode("addresses[][address]", "utf-8"));
//                formatedParameter.append("=");
//                formatedParameter.append(URLEncoder.encode(addressList.get(i).shortAddress, "utf-8"));
//                formatedParameter.append("&");
//                formatedParameter.append(URLEncoder.encode("addresses[][region_id]", "utf-8"));
//                formatedParameter.append("=");
//                formatedParameter.append(URLEncoder.encode(addressList.get(i).regionId + "", "utf-8"));
//                formatedParameter.append("&");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        parameters.put("addition[shelf_id]", shelf_id + "");
        return request("task/do_task", parameters);
    }

    public static JSONObject order_get_price(int order_id) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("order_id", order_id + "");
        return request("order/get_price", parameters);
    }
}
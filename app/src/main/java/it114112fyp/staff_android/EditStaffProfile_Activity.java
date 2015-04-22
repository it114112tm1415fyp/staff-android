package it114112fyp.staff_android;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import it114112fyp.util.AddressItem;
import it114112fyp.util.DataList;
import it114112fyp.util.StaffData;

public class EditStaffProfile_Activity extends Activity {

    private ProgressDialog progressDialog;

    private LinearLayout layLinearAddress;
    private ImageButton imgBtnAddAddress;
    private Button btnSubmit;
    private TextView txtViewUserId, txtViewUsername;
    private EditText edTxtRealName, edTxtPhone, edTxtNewPassword, edTxtConfirmPassword, edTxtEmail;

    private ArrayList<AddressItem> addressItemList;
    private String realName, phone, email, newPassword, rePassword;
    private int layLinearChildCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_staff_profile);
        findAllView();
        realName = StaffData.name;
        phone = StaffData.phone.substring(5);
        email = StaffData.email;
        if (StaffData.regionList.size() == 0) {
            new Thread(sendGetRegionListRequest).start();
        } else {
            setAllView();
        }
    }

    private void findAllView() {
        layLinearAddress = (LinearLayout) findViewById(R.id.layLinearAddress);
        txtViewUserId = (TextView) findViewById(R.id.tvUserId);
        txtViewUsername = (TextView) findViewById(R.id.txtViewUsername);
        edTxtRealName = (EditText) findViewById(R.id.edTxtRealName);
        edTxtEmail = (EditText) findViewById(R.id.edTxtEmail);
        edTxtPhone = (EditText) findViewById(R.id.edTxtPhone);
        edTxtNewPassword = (EditText) findViewById(R.id.edTxtNewPassword);
        edTxtConfirmPassword = (EditText) findViewById(R.id.edTxtConfirmPassword);
        imgBtnAddAddress = (ImageButton) findViewById(R.id.imgBtnAddAddress);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
    }

    private void setAllView() {
        txtViewUserId.setText("" + StaffData.id);
        txtViewUsername.setText(StaffData.username);
        edTxtRealName.setText(realName);
        edTxtPhone.setText(phone);
        edTxtEmail.setText(email);
        JSONArray addressArray = StaffData.addresses;
        for (int i = 0; i < addressArray.length(); i++) {
            JSONObject addressObject;
            try {
                addressObject = addressArray.getJSONObject(i);
                JSONObject regionObject = addressObject.getJSONObject("region");
                Log.e("regionId", regionObject.getInt("id") + "");
                Log.e("indexOfRegionId", StaffData.regionList.indexOfId(regionObject.getInt("id")) + "");
                createAddressView(addressObject.getString("short_name"), StaffData.regionList.indexOfId(regionObject.getInt("id")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        imgBtnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddressView("", -1);
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPassword = edTxtNewPassword.getText().toString();
                rePassword = edTxtConfirmPassword.getText().toString();
                realName = edTxtRealName.getText().toString();
                email = edTxtEmail.getText().toString();
                phone = edTxtPhone.getText().toString();
                addressItemList = new ArrayList<>();
                layLinearChildCount = layLinearAddress.getChildCount();
                for (int i = 0; i < layLinearChildCount; i++) {
                    EditText inner_ed1 = (EditText) layLinearAddress.getChildAt(i).findViewById(R.id.edTxtSubAddress1);
                    EditText inner_ed2 = (EditText) layLinearAddress.getChildAt(i).findViewById(R.id.edTxtSubAddress2);
                    EditText inner_ed3 = (EditText) layLinearAddress.getChildAt(i).findViewById(R.id.edTxtSubAddress3);
                    Spinner innerSpnnrRegion = (Spinner) layLinearAddress.getChildAt(i).findViewById(R.id.spnnrRegion);
                    String subAddress1 = inner_ed1.getText().toString();
                    String subAddress2 = inner_ed2.getText().toString();
                    String subAddress3 = inner_ed3.getText().toString();
                    // Check Address
                    if (subAddress1.equals("") || subAddress2.equals("") || subAddress3.equals("")
                            || innerSpnnrRegion.getSelectedItem().toString().equals("--")) {
                        Toast.makeText(getApplicationContext(), "Address is not complete!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int regionId = StaffData.regionList.idOfIndex(innerSpnnrRegion.getSelectedItemPosition());
                    if (!subAddress1.substring(subAddress1.length() - 1).equals(","))
                        subAddress1 = subAddress1 + ",";
                    if (!subAddress2.substring(subAddress2.length() - 1).equals(","))
                        subAddress2 = subAddress2 + ",";
                    AddressItem addressItem = new AddressItem(0, subAddress1 + "\n" + subAddress2 + "\n" + subAddress3, "", regionId,
                            innerSpnnrRegion.getSelectedItem().toString());
                    addressItemList.add(addressItem);
                }
                // Check data complete or not
                if (realName.equals("") || email.equals("") || phone.equals("") || addressItemList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Please finish the form!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check phone format
                phone = "+852-" + phone;
                String phoneFormat = getResources().getString(R.string.format_phone);
                if (!phone.matches(phoneFormat)) {
                    Toast.makeText(getApplicationContext(), "Phone is invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check password empty or not
                if (!newPassword.equals("") || !rePassword.equals("")) {
                    // Check new password match to re password or not
                    if (!newPassword.equals(rePassword)) {
                        Toast.makeText(getApplicationContext(), "New password not match!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPassword.length() < 8 || rePassword.length() < 8) {
                        Toast.makeText(getApplicationContext(), "Password is too short!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (progressDialog == null)
                    progressDialog = ProgressDialog.show(EditStaffProfile_Activity.this, "Loading...", "Sending Edit Staff Request...", true);
                // Send edit profile request
                new Thread(sendEditProfileRequest).start();
            }
        });
    }

    private void createAddressView(String address, final int indexOfRegionId) {
        layLinearChildCount = layLinearAddress.getChildCount();
        if (layLinearChildCount < 6) {
            LayoutInflater LayInf = LayoutInflater.from(getApplicationContext());
            View view = LayInf.inflate(R.layout.add_address_item, null);
            layLinearAddress.addView(view, 0);
            String[] addressList = new String[3];
            if (!address.equals("")) {
                addressList = address.split("\n");
                EditText innerEdTxtSubAddress1 = (EditText) view.findViewById(R.id.edTxtSubAddress1);
                EditText innerEdTxtSubAddress2 = (EditText) view.findViewById(R.id.edTxtSubAddress2);
                EditText innerEdTxtSubAddress3 = (EditText) view.findViewById(R.id.edTxtSubAddress3);
                innerEdTxtSubAddress1.setText(addressList[0]);
                innerEdTxtSubAddress2.setText(addressList[1]);
                innerEdTxtSubAddress3.setText(addressList[2]);
            }
            final Spinner innerSpnnrRegion = (Spinner) view.findViewById(R.id.spnnrRegion);
            final DataList region = new DataList(StaffData.regionList);
            if (indexOfRegionId == -1) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", 0);
                map.put("name", "--");
                region.add(0, map);
            }
            final SimpleAdapter simpleAdapter = new SimpleAdapter(
                    getApplicationContext(), region, android.R.layout.simple_list_item_1,
                    new String[]{"name"}, new int[]{android.R.id.text1}) {

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    view.setBackgroundColor(Color.WHITE);
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.BLACK);
                    return view;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    view.setBackgroundColor(Color.WHITE);
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.BLACK);
                    return view;
                }
            };
            innerSpnnrRegion.setAdapter(simpleAdapter);
            innerSpnnrRegion.setSelection(indexOfRegionId);
            innerSpnnrRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0)
                        if (indexOfRegionId == -1)
                            if (region.size() > StaffData.regionList.size()) {
                                region.remove(0);
                                simpleAdapter.notifyDataSetChanged();
                                innerSpnnrRegion.setSelection(position - 1);
                            }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            ImageButton innerImgBtn = (ImageButton) view.findViewById(R.id.imgBtnRemoveAddress);
            innerImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewGroup) v.getParent().getParent()).removeView((View) v.getParent());
                    if (layLinearAddress.getChildCount() == 0) {
                        createAddressView("", 0);
                    }
                }
            });
        }
    }

    Thread sendEditProfileRequest = new Thread() {
        public void run() {
            final JSONObject result = HTTPRequest.account_edit_profile(
                    StaffData.password, newPassword, realName, phone, email, addressItemList);
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        // Save user data
                        if (!newPassword.equals(""))
                            StaffData.password = newPassword;
                        JSONObject content = result.getJSONObject("content");
                        StaffData.name = content.getString("name");
                        StaffData.email = content.getString("email");
                        StaffData.phone = content.getString("phone");
                        StaffData.register_time = DateFormator.getDateTime(content.getString("created_at"));
                        StaffData.last_modify_time = DateFormator.getDateTime(content.getString("updated_at"));
                        StaffData.addresses = content.getJSONArray("specify_addresses");
                        StaffData.staffObject = content;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    } else
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), result.getString("error"), Toast.LENGTH_SHORT)
                                            .show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };

    Thread sendGetRegionListRequest = new Thread() {
        public void run() {
            final JSONObject result = HTTPRequest.region_get_list();
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        JSONArray list = result.getJSONObject("content").getJSONArray("list");
                        StaffData.regionList.clear();
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject region = list.getJSONObject(i);
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", region.getInt("id"));
                            map.put("name", region.getString("name"));
                            StaffData.regionList.add(map);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setAllView();
                            }
                        });
                    } else {
                        try {
                            String error = result.getString("error");
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Fail to server. \n Please try again later.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Can not get region", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }
    };

}

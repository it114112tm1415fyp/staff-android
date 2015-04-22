package it114112fyp.staff_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class BeltController_Activity extends Activity {

    private ArrayList<HashMap<String, ToggleButton>> chView = new ArrayList<>();
    private ArrayList<HashMap<String, ToggleButton>> crView = new ArrayList<>();
    private ArrayList<ToggleButton> stView = new ArrayList<>();
    private HashMap<String, ToggleButton> mrView = new HashMap<>();

    public int id;
    public JSONObject message;

    GetControl efsd = new GetControl();

    private final OnClickListener ToggleButtonOnClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            BeltController_Activity.OnToggleInGroup(v);
            BeltController_Activity.SendMessage ds = new SendMessage();
            ds.view = v;
            ds.start();
        }
    };

    private final OnClickListener UpDownOnClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            BeltController_Activity.SendMessage ds = new SendMessage();
            ds.view = v;
            ds.start();
        }
    };

    static HashMap<Integer, String> IDTable = new HashMap<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.belt_controllor_activity);
        Intent self = getIntent();
        id = self.getIntExtra("id", 0);
        message = ConveyorBeltList_Activity.message;
        Class<?> RidClass = R.id.class;
        Field[] field = RidClass.getFields();
        for (Field x : field) {
            try {
                IDTable.put(x.getInt(null), x.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        HashMap<String, ToggleButton> ch1 = new HashMap<>();
        ch1.put("Backward", (ToggleButton) findViewById(R.id.CH1_Backward));
        ch1.put("Forward", (ToggleButton) findViewById(R.id.CH1_Forward));
        ch1.put("Stop", (ToggleButton) findViewById(R.id.CH1_Stop));
        ch1.put("Up", (ToggleButton) findViewById(R.id.CH1_Up));

        HashMap<String, ToggleButton> ch2 = new HashMap<>();
        ch2.put("Backward", (ToggleButton) findViewById(R.id.CH2_Backward));
        ch2.put("Forward", (ToggleButton) findViewById(R.id.CH2_Forward));
        ch2.put("Stop", (ToggleButton) findViewById(R.id.CH2_Stop));
        ch2.put("Up", (ToggleButton) findViewById(R.id.CH2_Up));

        HashMap<String, ToggleButton> ch3 = new HashMap<>();
        ch3.put("Backward", (ToggleButton) findViewById(R.id.CH3_Backward));
        ch3.put("Forward", (ToggleButton) findViewById(R.id.CH3_Forward));
        ch3.put("Stop", (ToggleButton) findViewById(R.id.CH3_Stop));
        ch3.put("Up", (ToggleButton) findViewById(R.id.CH3_Up));

        HashMap<String, ToggleButton> ch4 = new HashMap<>();
        ch4.put("Backward", (ToggleButton) findViewById(R.id.CH4_Backward));
        ch4.put("Forward", (ToggleButton) findViewById(R.id.CH4_Forward));
        ch4.put("Stop", (ToggleButton) findViewById(R.id.CH4_Stop));
        ch4.put("Up", (ToggleButton) findViewById(R.id.CH4_Up));

        chView.add(ch1);
        chView.add(ch2);
        chView.add(ch3);
        chView.add(ch4);

        HashMap<String, ToggleButton> cr1 = new HashMap<String, ToggleButton>();
        cr1.put("Backward", (ToggleButton) findViewById(R.id.CR1_Backward));
        cr1.put("Forward", (ToggleButton) findViewById(R.id.CR1_Forward));
        cr1.put("Stop", (ToggleButton) findViewById(R.id.CR1_Stop));

        HashMap<String, ToggleButton> cr2 = new HashMap<String, ToggleButton>();
        cr2.put("Backward", (ToggleButton) findViewById(R.id.CR2_Backward));
        cr2.put("Forward", (ToggleButton) findViewById(R.id.CR2_Forward));
        cr2.put("Stop", (ToggleButton) findViewById(R.id.CR2_Stop));

        crView.add(cr1);
        crView.add(cr2);

        stView.add((ToggleButton) findViewById(R.id.ST1_Up));
        stView.add((ToggleButton) findViewById(R.id.ST2_Up));
        stView.add((ToggleButton) findViewById(R.id.ST3_Up));
        stView.add((ToggleButton) findViewById(R.id.ST4_Up));
        stView.add((ToggleButton) findViewById(R.id.ST5_Up));
        stView.add((ToggleButton) findViewById(R.id.ST6_Up));
        stView.add((ToggleButton) findViewById(R.id.ST7_Up));
        stView.add((ToggleButton) findViewById(R.id.ST8_Up));

        mrView.put("Anticlockwise", (ToggleButton) findViewById(R.id.MR1_Anticlockwise));
        mrView.put("Clockwise", (ToggleButton) findViewById(R.id.MR1_Clockwise));
        mrView.put("Stop", (ToggleButton) findViewById(R.id.MR1_Stop));

        for (ToggleButton x : mrView.values()) {
            x.setOnClickListener(ToggleButtonOnClick);
        }
        for (HashMap<String, ToggleButton> x1 : chView) {
            for (ToggleButton x2 : x1.values()) {
                x2.setOnClickListener(ToggleButtonOnClick);
            }
            x1.get("Up").setOnClickListener(UpDownOnClick);
        }
        for (HashMap<String, ToggleButton> x1 : crView) {
            for (ToggleButton x2 : x1.values()) {
                x2.setOnClickListener(ToggleButtonOnClick);
            }
        }
        for (ToggleButton x : stView) {
            x.setOnClickListener(UpDownOnClick);
        }
        ChangeButtonView(message);
    }

    @Override
    public void onStart() {
        super.onStart();
        efsd = new GetControl();
        efsd.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        efsd.stop = true;
    }

    public void ChangeButtonView(final JSONObject message) {
        runOnUiThread(new Thread() {

            public void run() {
                try {
                    JSONArray ch = message.getJSONArray("ch");
                    for (int t = 0; t < ch.length(); t++) {
                        switch (ch.getInt(t) / 2) {
                            case 0:
                                OnToggleInGroup(chView.get(t).get("Stop"));
                                break;
                            case 1:
                                OnToggleInGroup(chView.get(t).get("Forward"));
                                break;
                            case 2:
                                OnToggleInGroup(chView.get(t).get("Backward"));
                                break;
                            default:
                                throw (new Exception());
                        }
                        switch (ch.getInt(t) % 2) {
                            case 0:
                                chView.get(t).get("Up").setChecked(false);
                                break;
                            case 1:
                                chView.get(t).get("Up").setChecked(true);
                                break;
                            default:
                                throw (new Exception());
                        }
                        Log.d("message", "" + chView.get(t));
                    }

                    JSONArray cr = message.getJSONArray("cr");
                    for (int t = 0; t < cr.length(); t++) {
                        switch (cr.getInt(t)) {
                            case 0:
                                OnToggleInGroup(crView.get(t).get("Stop"));
                                break;
                            case 1:
                                OnToggleInGroup(crView.get(t).get("Forward"));
                                break;
                            case 2:
                                OnToggleInGroup(crView.get(t).get("Backward"));
                                break;
                            default:
                                throw (new Exception());
                        }
                    }
                    JSONArray st = message.getJSONArray("st");
                    for (int t = 0; t < st.length(); t++) {
                        switch (st.getInt(t)) {
                            case 0:
                                stView.get(t).setChecked(false);
                                break;
                            case 1:
                                stView.get(t).setChecked(true);
                                break;
                            default:
                                throw (new Exception());
                        }
                    }
                    switch (message.getInt("mr")) {
                        case 0:
                            OnToggleInGroup(mrView.get("Stop"));
                            break;
                        case 1:
                            OnToggleInGroup(mrView.get("Clockwise"));
                            break;
                        case 2:
                            OnToggleInGroup(mrView.get("Anticlockwise"));
                            break;
                        default:
                            throw (new Exception());
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public static void OnToggleInGroup(View view) {
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        String tag = view.getTag().toString();
        int count = (BeltController_Activity.getViewsByTag(viewGroup, tag)).size();
        ArrayList<View> tagView = BeltController_Activity.getViewsByTag(viewGroup, tag);
        for (int i = 0; i < count; i++) {
            ToggleButton togglebutton = (ToggleButton) viewGroup.findViewById(tagView.get(i)
                    .getId());
            togglebutton.setChecked(false);
        }
        ToggleButton togglebutton = (ToggleButton) viewGroup.findViewById(view.getId());
        togglebutton.setChecked(true);
    }

    class GetControl extends Thread {
        public boolean stop;

        public void run() {

            stop = false;
            while (!stop) {
                try {
                    Thread.sleep(5000);
                    JSONObject result = HTTPRequest.get_control(id);
                    if (result.getBoolean("success")) {
                        ChangeButtonView(result.getJSONObject("message"));
                    } else {
                        final String error = result.getString("error");
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    class SendMessage extends Thread {

        public View view;

        public void run() {
            final JSONObject result = HTTPRequest.send_message(id, IDTable.get(view.getId()));
            Log.d("Button click", "" + IDTable.get(view.getId()));
            try {
                if (result.getBoolean("success")) {
                    ChangeButtonView(result.getJSONObject("message"));
                } else {
                    final String error = result.getString("error");
                    runOnUiThread(new Thread() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("error", "");
            }
        }
    }

    static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
        return views;
    }

}

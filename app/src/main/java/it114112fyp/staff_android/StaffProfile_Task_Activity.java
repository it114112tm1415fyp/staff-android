package it114112fyp.staff_android;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import it114112fyp.util.StaffData;

public class StaffProfile_Task_Activity extends Activity {

    private ProgressDialog progressDialog;

    private SimpleAdapter taskInfoAdapter;

    private LinearLayout layLinearAddress;
    private ListView taskListView;
    private TextView _staffID, _staffName, _staffPhone, _staffEmail, txtViewWorkplace, _staffRegisterDate, _staffLastModifyDate;
    private Button editButton, btnRefresh;

    private ArrayList<HashMap<String, Object>> taskInfo = new ArrayList<>();
    private int conveyorId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_profile_task);
        findAllView();
        setAllView();
        progressDialog = ProgressDialog.show(StaffProfile_Task_Activity.this, "Loading...", "Getting Today Task...", true);
        new Thread(SendGetTodayTaskRequest).start();
        if (StaffData.regionList.size() == 0)
            new Thread(SendGetRegionListRequest).start();
    }

    @Override
    public void onBackPressed() {
        if (!StaffData.username.equals("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(StaffProfile_Task_Activity.this);
            alertDialog.setTitle("Do you want exit?");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case 1:
                    setTextView();
                    break;
                case 2:
                    progressDialog = ProgressDialog.show(StaffProfile_Task_Activity.this, "Loading...", "Getting Today Task...", true);
                    new Thread(SendGetTodayTaskRequest).start();
                    break;
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh Today Task");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                new Thread(SendGetTodayTaskRequest).start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findAllView() {
        layLinearAddress = (LinearLayout) findViewById(R.id.layLinearStaffAddress);
        _staffID = (TextView) findViewById(R.id.staffIdTv);
        _staffName = (TextView) findViewById(R.id.staffNameTv);
        _staffPhone = (TextView) findViewById(R.id.staffPhoneTv);
        _staffEmail = (TextView) findViewById(R.id.staffEmailTv);
        txtViewWorkplace = (TextView) findViewById(R.id.txtViewWorkspace);
        _staffRegisterDate = (TextView) findViewById(R.id.staffRegisterDate);
        _staffLastModifyDate = (TextView) findViewById(R.id.staffLastModifyDate);
        taskListView = (ListView) findViewById(R.id.taskListView);
        editButton = (Button) findViewById(R.id.editPageButton);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
    }

    private void setAllView() {
        setTextView();
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), EditStaffProfile_Activity.class), 1);
            }
        });
        btnRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(StaffProfile_Task_Activity.this, "Loading...", "Getting Today Task...", true);
                new Thread(SendGetTodayTaskRequest).start();
            }
        });
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent();
                try {
                    String taskType = StaffData.todayTask.get(position).getString("type");
                    if (taskType.contains("ServeTask")) {
                        i = new Intent(getApplicationContext(), ShopFunction_Activity.class);
                        i.putExtra("taskId", StaffData.todayTask.get(position).getInt("id"));
                    } else {
                        i = new Intent(getApplicationContext(), TaskDetails_Activity.class);
                        i.putExtra("taskId", StaffData.todayTask.get(position).getInt("id"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivityForResult(i, 2);
            }
        });
        taskInfoAdapter = new SimpleAdapter(
                getApplicationContext(), taskInfo, R.layout.tasks_list_item,
                new String[]{"type_action", "completed", "time"},
                new int[]{R.id.txtViewTask_Action, R.id.txtViewTaskComplete, R.id.txtViewTaskTime}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                HashMap<String, Object> taskMap = taskInfo.get(position);
                boolean completed = (boolean) taskMap.get("completed");
                boolean canDo = (boolean) taskMap.get("can_do");
                if (!canDo && !completed)
                    view.setBackgroundColor(Color.rgb(220, 220, 220));
                else if (!completed)
                    view.setBackgroundColor(Color.rgb(255, 255, 160));
                else
                    view.setBackgroundColor(Color.rgb(160, 255, 160));
                return view;
            }
        };
        taskListView.setAdapter(taskInfoAdapter);
    }

    private void setTextView() {
        _staffID.setText("" + StaffData.id);
        _staffName.setText(StaffData.name);
        _staffPhone.setText(StaffData.phone);
        _staffEmail.setText(StaffData.email);
        txtViewWorkplace.setText(StaffData.workplace);
        layLinearAddress.removeAllViews();
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < StaffData.addresses.length(); i++) {
            try {
                TextView textView = new TextView(getApplicationContext());
                textView.setText(StaffData.addresses.getJSONObject(i).getString("long_name"));
                textView.setTextSize(20);
                textView.setTextColor(Color.BLACK);
                textView.setPadding(0, 0, 0, 10);
                layLinearAddress.addView(textView, p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        _staffRegisterDate.setText(StaffData.last_modify_time);
        _staffLastModifyDate.setText(StaffData.register_time);
    }

    Thread SendGetTodayTaskRequest = new Thread() {
        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.task_get_today_task();
                if (result != null) {
                    if (result.getBoolean("success")) {
                        final JSONArray tasksArray = result.getJSONArray("content");
                        if (tasksArray.length() != 0) {
                            StaffData.todayTask.clear();
                            taskInfo.clear();
                            for (int i = 0; i < tasksArray.length(); i++) {
                                JSONObject taskObject = tasksArray.getJSONObject(i);
                                String taskType = taskObject.getString("type");
                                String taskAction = taskObject.getString("check_action");
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("completed", taskObject.optBoolean("completed", false));
                                map.put("type_action", taskType + " - " + taskAction);
                                map.put("time", DateFormator.getTime(taskObject.getString("datetime")));
                                map.put("can_do", taskObject.getBoolean("can_do"));
                                taskInfo.add(map);
                                StaffData.todayTask.add(taskObject);
                                if (taskType.equals("ServeTask") && taskAction.equals("receive")) {
                                    StaffData.SERVE_TASK_RECEIVE = taskObject.getInt("id");
                                } else if (taskType.equals("ServeTask") && taskAction.equals("issue")) {
                                    StaffData.SERVE_TASK_ISSUE = taskObject.getInt("id");
                                }
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "No Task", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), result.getString("error"),
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), "Fail to connect server",
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        taskInfoAdapter.notifyDataSetChanged();
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };

    Thread SendGetRegionListRequest = new Thread() {
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
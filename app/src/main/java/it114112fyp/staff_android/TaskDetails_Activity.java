package it114112fyp.staff_android;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it114112fyp.util.StaffData;

public class TaskDetails_Activity extends Activity {

    public static int taskId;
    public static String taskType, taskAction;
    public static JSONArray goodsInTask, orderInTask, orderQueues;
    public static JSONObject orderInTaskObject;

    private SimpleAdapter orderInTaskAdapter;

    private ProgressDialog progressDialog;

    private TableLayout layTableOrderQueues, layTable;
    private LinearLayout layLinearOrderInTask;
    private ListView lstOrderInTask;
    private TextView txtViewTaskId, txtViewTaskType, txtViewAction, txtViewComplete, txtViewTaskTime,
            txtViewOrderId, txtViewSender, txtViewReceiver, txtViewDeparture, txtViewDestination, txtViewPhone;
    private Button btnDoTask, btnStartAllocation, btnStopAllocation, btnAvailable, btnNotAvailable;

    private List<HashMap<String, Object>> orderInTaskList = new ArrayList();
    private List<HashMap<String, Object>> orderQueuesList = new ArrayList();

    private Boolean complete = false, taskCanDo = false;
    private String time = "";
    private int orderId, conveyorId, tableChildCount;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details);
        Intent self = getIntent();
        taskId = self.getIntExtra("taskId", 0);
        findAllView();
        setAllView();
        progressDialog = ProgressDialog.show(TaskDetails_Activity.this, "Loading...", "Getting Task Details ...", true);
        new SendGetTaskDetailsRequest().start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            progressDialog = ProgressDialog.show(TaskDetails_Activity.this, "Loading...", "Getting Task Details ...", true);
            new SendGetTaskDetailsRequest().start();
            setResult(RESULT_OK);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh Task");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                progressDialog = ProgressDialog.show(TaskDetails_Activity.this, "Loading...", "Getting Task Details ...", true);
                new SendGetTaskDetailsRequest().start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findAllView() {
        layTable = (TableLayout) findViewById(R.id.layTable);
        layTableOrderQueues = (TableLayout) findViewById(R.id.layTableOrderQueues);
        layLinearOrderInTask = (LinearLayout) findViewById(R.id.layLinearOrderInTask);
        lstOrderInTask = (ListView) findViewById(R.id.lstOrderInTask);
        txtViewTaskId = (TextView) findViewById(R.id.taskTaskIdTv);
        txtViewTaskType = (TextView) findViewById(R.id.taskTaskTypeTv);
        txtViewAction = (TextView) findViewById(R.id.taskActionTv);
        txtViewComplete = (TextView) findViewById(R.id.taskCompleteTv);
        txtViewTaskTime = (TextView) findViewById(R.id.taskTimeTv);
        btnDoTask = (Button) findViewById(R.id.btnDoTask);
        btnStartAllocation = (Button) findViewById(R.id.btnStartAllocation);
        btnStopAllocation = (Button) findViewById(R.id.btnStopAllocation);
        txtViewOrderId = (TextView) findViewById(R.id.txtViewOrderId);
        txtViewSender = (TextView) findViewById(R.id.txtViewSender);
        txtViewReceiver = (TextView) findViewById(R.id.txtViewReceiver);
        txtViewDeparture = (TextView) findViewById(R.id.txtViewDeparture);
        txtViewDestination = (TextView) findViewById(R.id.txtViewDestination);
        txtViewPhone = (TextView) findViewById(R.id.txtViewPhone);
        btnAvailable = (Button) findViewById(R.id.btnAvailable);
        btnNotAvailable = (Button) findViewById(R.id.btnNotAvailable);
        tableChildCount = layTable.getChildCount();
    }

    private void setAllView() {
        btnStartAllocation.setVisibility(View.GONE);
        btnStopAllocation.setVisibility(View.GONE);
        btnDoTask.setVisibility(View.GONE);
        layTableOrderQueues.setVisibility(View.GONE);
        layLinearOrderInTask.setVisibility(View.INVISIBLE);
        btnDoTask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LogTransferRecord_Activity.class);
                i.putExtra("taskId", taskId);
                i.putExtra("orderId", orderId);
                i.putExtra("action", taskAction);
                i.putExtra("activity", "Task");
                startActivityForResult(i, 1);
            }
        });
        orderInTaskAdapter = new SimpleAdapter(
                getApplicationContext(), orderInTaskList, R.layout.order_in_task_list_item,
                new String[]{"order_id", "order_state", "completed"},
                new int[]{R.id.txtViewOrderId, R.id.txtViewOrderState, R.id.txtViewOrderComplete}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor((boolean) orderInTaskList.get(position).get("completed") ? Color.rgb(160, 255, 160) : Color.rgb(255, 255, 160));
                return view;
            }
        };
        lstOrderInTask.setAdapter(orderInTaskAdapter);
        lstOrderInTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    orderInTaskObject = orderInTask.getJSONObject(position);
                    Log.e("orderInTask", orderInTaskObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (taskType.equals("IssueTask")) {
                    Intent i = new Intent(getApplicationContext(), IssueOrder_Activity.class);
                    i.putExtra("activity", "Task");
                    i.putExtra("orderId", (int) orderInTaskList.get(position).get("order_id"));
                    i.putExtra("taskId", taskId);
                    i.putExtra("action", taskAction);
                    startActivityForResult(i, 1);
                } else if (taskType.equals("ReceiveTask")) {
                    Intent i = new Intent(getApplicationContext(), ReceiveOrder_Activity.class);
                    i.putExtra("activity", "Task");
                    i.putExtra("orderId", (int) orderInTaskList.get(position).get("order_id"));
                    i.putExtra("taskId", taskId);
                    i.putExtra("action", taskAction);
                    startActivityForResult(i, 1);
                }
            }
        });
        btnAvailable.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(TaskDetails_Activity.this, "Loading...", "Sending Contact Request...", true);
                new SendContactRequest(taskId, orderId, true).start();
            }
        });
        btnNotAvailable.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(TaskDetails_Activity.this, "Loading...", "Sending Contact Request...", true);
                new SendContactRequest(taskId, orderId, false).start();
            }
        });
        btnStartAllocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(TaskDetails_Activity.this, "Loading...", "Sending Start Allocation Request...", true);
                new SendStartAllocationRequest(StaffData.conveyorId, "auto_start").start();
            }
        });
        btnStopAllocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(TaskDetails_Activity.this, "Loading...", "Sending Stop Allocation Request...", true);
                new SendStopAllocationRequest(StaffData.conveyorId, "auto_stop").start();
            }
        });
        setTextView();
    }

    private void setTextView() {
        txtViewTaskId.setText(taskId + "");
        txtViewTaskType.setText(taskType);
        txtViewAction.setText(taskAction);
        if (complete)
            txtViewComplete.setTextColor(Color.GREEN);
        else
            txtViewComplete.setTextColor(Color.RED);
        txtViewComplete.setText(complete + "");
        txtViewTaskTime.setText(time);
    }

    private TableRow createTableRow(Context context, String title, String content) {
        TableRow tableRow = new TableRow(context);
        TextView textView1 = new TextView(context);
        textView1.setText(title);
        textView1.setTextSize(20);
        textView1.setPadding(10, 10, 10, 10);
        textView1.setTextColor(Color.BLACK);
        TextView textView2 = new TextView(context);
        textView2.setText(content);
        textView2.setTextSize(20);
        textView2.setPadding(10, 10, 10, 10);
        textView2.setTextColor(Color.BLACK);
        tableRow.addView(textView1);
        tableRow.addView(textView2);
        return tableRow;
    }

    class SendGetTaskDetailsRequest extends Thread {
        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.task_get_details(taskId);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        final JSONObject taskObject = result.getJSONObject("content");
                        taskType = taskObject.getString("type");
                        taskAction = taskObject.getString("check_action");
                        taskCanDo = taskObject.getBoolean("can_do");
                        complete = taskObject.getBoolean("completed");
                        orderInTaskList.clear();
                        orderQueuesList.clear();
                        if (taskType.equals("ReceiveTask") || taskType.equals("IssueTask")) {
                            JSONArray orderInTask = taskObject.getJSONArray("order_in_task");
                            TaskDetails_Activity.orderInTask = orderInTask;
                            for (int i = 0; i < orderInTask.length(); i++) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("order_id", orderInTask.getJSONObject(i).getInt("order_id"));
                                map.put("order_state", orderInTask.getJSONObject(i).getString("order_state"));
                                map.put("completed", orderInTask.getJSONObject(i).getBoolean("completed"));
                                orderInTaskList.add(map);
                            }
                            orderQueues = taskObject.getJSONArray("order_queues");
                            goodsInTask = taskObject.getJSONArray("goods_in_task");
                        } else if (taskType.equals("InspectTask") || taskType.equals("TransferTask")) {
                            goodsInTask = taskObject.getJSONArray("goods_in_task");
                            orderQueues = null;
                        } else {
                            goodsInTask = null;
                            orderQueues = null;
                        }
                        time = DateFormator.getDateTime(taskObject.getString("datetime"));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Show Task Details
                                    if (taskAction.equals("warehouse") && StaffData.conveyorId != 0) {
                                        btnStartAllocation.setVisibility(View.VISIBLE);
                                        btnStopAllocation.setVisibility(View.VISIBLE);
                                    } else {
                                        btnStartAllocation.setVisibility(View.INVISIBLE);
                                        btnStopAllocation.setVisibility(View.INVISIBLE);
                                    }
                                    if (!taskCanDo) {
                                        layTableOrderQueues.setVisibility(View.INVISIBLE);
                                        layLinearOrderInTask.setVisibility(View.GONE);
                                        btnDoTask.setVisibility(View.INVISIBLE);
                                    } else if (complete) {
                                        layTableOrderQueues.setVisibility(View.GONE);
                                        btnDoTask.setVisibility(View.INVISIBLE);
                                        if (taskType.equals("ReceiveTask") || taskType.equals("IssueTask")) {
                                            layLinearOrderInTask.setVisibility(View.VISIBLE);
                                        } else
                                            layLinearOrderInTask.setVisibility(View.INVISIBLE);
                                    } else {
                                        if (taskType.equals("InspectTask")) {
                                            layLinearOrderInTask.setVisibility(View.INVISIBLE);
                                            layTableOrderQueues.setVisibility(View.GONE);
                                            btnDoTask.setVisibility(View.VISIBLE);
                                        } else if (taskType.equals("TransferTask")) {
                                            layLinearOrderInTask.setVisibility(View.INVISIBLE);
                                            layTableOrderQueues.setVisibility(View.GONE);
                                            btnDoTask.setVisibility(View.VISIBLE);
                                        } else if (taskType.equals("ReceiveTask") || taskType.equals("IssueTask")) {
                                            if (taskAction.equals("contact")) {
                                                btnDoTask.setVisibility(View.INVISIBLE);
                                                layLinearOrderInTask.setVisibility(View.GONE);
                                                if (TaskDetails_Activity.orderQueues.length() > 0) {
                                                    layTableOrderQueues.setVisibility(View.VISIBLE);
                                                    JSONObject orderQueuesObject = TaskDetails_Activity.orderQueues.getJSONObject(0);
                                                    orderId = orderQueuesObject.getInt("id");
                                                    txtViewOrderId.setText(orderId + "");
                                                    JSONObject senderObject = orderQueuesObject.getJSONObject("sender");
                                                    JSONObject receiverObject = orderQueuesObject.getJSONObject("receiver");
                                                    txtViewSender.setText(senderObject.getString("name"));
                                                    txtViewReceiver.setText(receiverObject.getString("name"));
                                                    txtViewPhone.setText(taskType.equals("IssueTask") ? receiverObject.getString("phone") : senderObject.getString("phone"));
                                                    txtViewDeparture.setText(orderQueuesObject.getJSONObject("departure").getString("short_name"));
                                                    txtViewDestination.setText(orderQueuesObject.getJSONObject("destination").getString("short_name"));
                                                } else {
                                                    layTableOrderQueues.setVisibility(View.GONE);
                                                }
                                            } else if (taskAction.equals("unload")) {
                                                layLinearOrderInTask.setVisibility(View.INVISIBLE);
                                                layTableOrderQueues.setVisibility(View.GONE);
                                                btnDoTask.setVisibility(View.VISIBLE);
                                            } else if (taskAction.equals("load")) {
                                                layLinearOrderInTask.setVisibility(View.INVISIBLE);
                                                layTableOrderQueues.setVisibility(View.GONE);
                                                btnDoTask.setVisibility(View.VISIBLE);
                                            } else if (taskAction.equals("leave")) {
                                                layTableOrderQueues.setVisibility(View.GONE);
                                                btnDoTask.setVisibility(View.VISIBLE);
                                                layLinearOrderInTask.setVisibility(View.INVISIBLE);
                                            } else if (taskAction.equals("warehouse")) {
                                                layTableOrderQueues.setVisibility(View.GONE);
                                                btnDoTask.setVisibility(View.VISIBLE);
                                                layLinearOrderInTask.setVisibility(View.INVISIBLE);
                                            } else if (taskAction.equals("issue") || taskAction.equals("receive")) {
                                                layTableOrderQueues.setVisibility(View.GONE);
                                                layLinearOrderInTask.setVisibility(View.VISIBLE);
                                                btnDoTask.setVisibility(View.INVISIBLE);
                                            }
                                        } else {
                                            layLinearOrderInTask.setVisibility(View.VISIBLE);
                                            layTableOrderQueues.setVisibility(View.GONE);
                                            btnDoTask.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    // Create Table Row
                                    TableLayout.LayoutParams p = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    p.setMargins(10, 10, 10, 10);
                                    int indexOfTable = 4;
                                    if (taskType.equals("InspectTask")) {
                                        for (int i = 0; i < layTable.getChildCount(); i++) {
                                            if (layTable.getChildCount() > tableChildCount)
                                                layTable.removeViewAt(layTable.getChildCount() - indexOfTable - 1);
                                        }
                                        JSONObject storeObject = taskObject.getJSONObject("store");
                                        layTable.addView(createTableRow(getApplicationContext(), "Store ID : ", storeObject.getInt("id") + ""), layTable.getChildCount() - indexOfTable, p);
                                        layTable.addView(createTableRow(getApplicationContext(), "Store : ", storeObject.getString("short_name")), layTable.getChildCount() - indexOfTable, p);
                                    } else if (taskType.equals("TransferTask")) {
                                        for (int i = 0; i < layTable.getChildCount(); i++) {
                                            if (layTable.getChildCount() > tableChildCount)
                                                layTable.removeViewAt(layTable.getChildCount() - indexOfTable - 1);
                                        }
                                        JSONObject carObject = taskObject.getJSONObject("car");
                                        layTable.addView(createTableRow(getApplicationContext(), "Car ID : ", carObject.getInt("id") + ""), layTable.getChildCount() - indexOfTable, p);
                                        layTable.addView(createTableRow(getApplicationContext(), "Car : ", carObject.getString("short_name")), layTable.getChildCount() - indexOfTable, p);
                                        JSONObject fromObject = taskObject.getJSONObject("from");
                                        layTable.addView(createTableRow(getApplicationContext(), "From : ", fromObject.getString("short_name")), layTable.getChildCount() - indexOfTable, p);
                                        JSONObject toObject = taskObject.getJSONObject("to");
                                        layTable.addView(createTableRow(getApplicationContext(), "To : ", toObject.getString("short_name")), layTable.getChildCount() - indexOfTable, p);
                                    } else if (taskType.equals("ReceiveTask") || taskType.equals("IssueTask")) {
                                        for (int i = 0; i < layTable.getChildCount(); i++) {
                                            if (layTable.getChildCount() > tableChildCount)
                                                layTable.removeViewAt(layTable.getChildCount() - indexOfTable - 1);
                                        }
                                        JSONObject carObject = taskObject.getJSONObject("car");
                                        layTable.addView(createTableRow(getApplicationContext(), "Car ID : ", carObject.getInt("id") + ""), layTable.getChildCount() - indexOfTable, p);
                                        layTable.addView(createTableRow(getApplicationContext(), "Car : ", carObject.getString("short_name")), layTable.getChildCount() - indexOfTable, p);
                                    } else if (taskType.equals("ServeTask")) {
                                        for (int i = 0; i < layTable.getChildCount(); i++) {
                                            if (layTable.getChildCount() > tableChildCount)
                                                layTable.removeViewAt(layTable.getChildCount() - indexOfTable - 1);
                                        }
                                        JSONObject shopObject = taskObject.getJSONObject("shop");
                                        layTable.addView(createTableRow(getApplicationContext(), "Shop : ", shopObject.getString("short_name") + ""), layTable.getChildCount() - indexOfTable, p);
                                    }
                                    orderInTaskAdapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(),
                                            result.getString("error"), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT)
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            setTextView();
                            orderInTaskAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    }


    class SendContactRequest extends Thread {
        int taskId, orderId;
        boolean free;

        public SendContactRequest(int taskId, int orderId, boolean free) {
            this.taskId = taskId;
            this.orderId = orderId;
            this.free = free;
        }

        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.task_contact(taskId, orderId, free);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                            }
                        });
                        setResult(RESULT_OK);
                        new SendGetTaskDetailsRequest().start();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(),
                                            result.getString("error"), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getApplicationContext(), "Fail connect server.\nPlease check your network status", Toast.LENGTH_SHORT)
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            setTextView();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    }

    class SendStartAllocationRequest extends Thread {
        int conveyorId;
        String message;

        public SendStartAllocationRequest(int conveyorId, String message) {
            this.conveyorId = conveyorId;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                final JSONObject getControllResult = HTTPRequest.get_control(conveyorId);
                if (getControllResult != null)
                    if (getControllResult.getBoolean("success")) {
                        final JSONObject allocationResult = HTTPRequest.send_message(conveyorId, message);
                        if (allocationResult != null) {
                            if (allocationResult.getBoolean("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Allocation Start", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Toast.makeText(getApplicationContext(), allocationResult.getString("error"), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(), "Fail connect server.\nPlease check your network status", Toast.LENGTH_SHORT)
                                                .show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    }

    class SendStopAllocationRequest extends Thread {
        int conveyorId;
        String message;

        public SendStopAllocationRequest(int conveyorId, String message) {
            this.conveyorId = conveyorId;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                final JSONObject getControllResult = HTTPRequest.get_control(conveyorId);
                if (getControllResult != null)
                    if (getControllResult.getBoolean("success")) {
                        final JSONObject allocationResult = HTTPRequest.send_message(conveyorId, message);
                        if (allocationResult != null) {
                            if (allocationResult.getBoolean("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Allocation Stop", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Toast.makeText(getApplicationContext(), allocationResult.getString("error"), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(), "Fail connect server.\nPlease check your network status", Toast.LENGTH_SHORT)
                                                .show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    }

}

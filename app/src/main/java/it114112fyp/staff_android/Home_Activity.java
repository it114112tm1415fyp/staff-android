package it114112fyp.staff_android;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class Home_Activity extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        TabHost.TabSpec tab1 = tabHost.newTabSpec("Staff Profile");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("More");
        tab1.setIndicator("Staff Profile");
        tab1.setContent(new Intent(this, StaffProfile_Task_Activity.class));
        tab3.setIndicator("More");
        tab3.setContent(new Intent(this, MoreFunction_Activity.class));
        tabHost.addTab(tab1);
        tabHost.addTab(tab3);
    }

}

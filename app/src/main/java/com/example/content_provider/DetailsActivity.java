package com.example.content_provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    private String phoneNumber;
    private ListView lvDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView tvHeader = findViewById(R.id.tvHeaderDetails);
        lvDetails = findViewById(R.id.lvDetails);

        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");
        tvHeader.setText("Chi tiết cho số: " + phoneNumber);

        Button btnShowMessages = findViewById(R.id.btnShowMessages);
        Button btnShowCallLog = findViewById(R.id.btnShowCallLog);

        btnShowMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMessages(phoneNumber);
            }
        });

        btnShowCallLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCallLog(phoneNumber);
            }
        });
    }

    private void loadMessages(String phoneNumber) {
        ArrayList<String> messageList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsCursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = ?", new String[]{phoneNumber}, null);

        if (smsCursor != null) {
            int bodyIndex = smsCursor.getColumnIndex("body");
            while (smsCursor.moveToNext()) {
                String messageBody = smsCursor.getString(bodyIndex);
                messageList.add("Tin nhắn: " + messageBody);
            }
            smsCursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        lvDetails.setAdapter(adapter);
    }

    private void loadCallLog(String phoneNumber) {
        ArrayList<String> callLogList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor callLogCursor = contentResolver.query(CallLog.Calls.CONTENT_URI,
                null, CallLog.Calls.NUMBER + " = ?", new String[]{phoneNumber}, CallLog.Calls.DATE + " DESC");

        if (callLogCursor != null) {
            int dateIndex = callLogCursor.getColumnIndex(CallLog.Calls.DATE);
            int typeIndex = callLogCursor.getColumnIndex(CallLog.Calls.TYPE);
            int durationIndex = callLogCursor.getColumnIndex(CallLog.Calls.DURATION);

            while (callLogCursor.moveToNext()) {
                String callDate = callLogCursor.getString(dateIndex);
                String callType = callLogCursor.getString(typeIndex);
                String callDuration = callLogCursor.getString(durationIndex);

                String callTypeString;
                switch (Integer.parseInt(callType)) {
                    case CallLog.Calls.INCOMING_TYPE:
                        callTypeString = "Cuộc gọi đến";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        callTypeString = "Cuộc gọi đi";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        callTypeString = "Cuộc gọi bị bỏ lỡ";
                        break;
                    default:
                        callTypeString = "Cuộc gọi khác";
                        break;
                }
                callLogList.add(callTypeString + " - Thời gian: " + callDuration + " giây - Ngày: " + callDate);
            }
            callLogCursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, callLogList);
        lvDetails.setAdapter(adapter);
    }
}
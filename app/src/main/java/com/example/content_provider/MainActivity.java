package com.example.content_provider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1;
    private static final int CALL_LOG_PERMISSION_CODE = 2;
    private static final int CONTACTS_PERMISSION_CODE = 3;

    private ListView lvContacts;
    private ArrayList<String> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Danh sách số điện thoại");

        lvContacts = findViewById(R.id.lvContacts);
        checkPermissions();

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneNumber = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("PHONE_NUMBER", phoneNumber);
                startActivity(intent);
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS
            }, SMS_PERMISSION_CODE);
        } else {
            loadContactsFromSMS();
        }
    }

    private void loadContactsFromSMS() {
        HashSet<String> contactSet = new HashSet<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsCursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                new String[]{"address"}, null, null, null);

        if (smsCursor != null) {
            int addressIndex = smsCursor.getColumnIndex("address");
            while (smsCursor.moveToNext()) {
                String address = smsCursor.getString(addressIndex);
                contactSet.add(address);
            }
            smsCursor.close();
        }

        contactList = new ArrayList<>(contactSet);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        lvContacts.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContactsFromSMS();
            } else {
                Toast.makeText(this, "Quyền đọc tin nhắn bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
}



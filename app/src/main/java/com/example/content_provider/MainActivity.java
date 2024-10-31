package com.example.content_provider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
    private ListView lvContacts;
    private ArrayList<String> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hiển thị tên và mã sinh viên
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Tên: Nguyễn Thị Bích Ngọc - MSV: 22115053122124");

        // Khởi tạo ListView để hiển thị các số điện thoại
        lvContacts = findViewById(R.id.lvContacts);

        // Kiểm tra và yêu cầu quyền đọc SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
        } else {
            loadContactsFromSMS();
        }

        // Xử lý khi người dùng chọn một số điện thoại
        lvContacts.setOnItemClickListener((parent, view, position, id) -> {
            String phoneNumber = (String) parent.getItemAtPosition(position);
            loadMessages(phoneNumber);
        });
    }

    // Hàm để lấy danh sách các số điện thoại từ tin nhắn đến
    private void loadContactsFromSMS() {
        HashSet<String> contactSet = new HashSet<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsCursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                new String[]{"address"}, null, null, null);

        if (smsCursor != null) {
            int addressIndex = smsCursor.getColumnIndex("address");
            while (smsCursor.moveToNext()) {
                String address = smsCursor.getString(addressIndex);
                contactSet.add(address); // Thêm số điện thoại vào tập hợp để loại bỏ trùng lặp
            }
            smsCursor.close();
        }

        contactList = new ArrayList<>(contactSet); // Chuyển tập hợp thành danh sách
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        lvContacts.setAdapter(adapter);
    }

    // Hàm để lấy các tin nhắn từ một số điện thoại nhất định
    private void loadMessages(String phoneNumber) {
        ArrayList<String> smsList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsCursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = ?", new String[]{phoneNumber}, null);

        if (smsCursor != null) {
            int bodyIndex = smsCursor.getColumnIndex("body");
            if (smsCursor.moveToFirst()) {
                do {
                    if (bodyIndex >= 0) {
                        String body = smsCursor.getString(bodyIndex);
                        smsList.add("Message: " + body);
                    }
                } while (smsCursor.moveToNext());
            }
            smsCursor.close();
        }

        // Nếu không có tin nhắn nào, hiển thị thông báo
        if (smsList.isEmpty()) {
            smsList.add("Không có tin nhắn nào từ " + phoneNumber);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsList);
        lvContacts.setAdapter(adapter);
    }

    // Xử lý kết quả yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContactsFromSMS();
            } else {
                Toast.makeText(this, "Quyền đọc tin nhắn bị từ chối", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Khi quay lại, khôi phục lại danh sách liên hệ
    @Override
    public void onBackPressed() {
        // Nếu đang xem tin nhắn, quay lại danh sách liên hệ
        if (lvContacts.getAdapter() != null && lvContacts.getAdapter().getCount() < contactList.size()) {
            loadContactsFromSMS(); // Khôi phục lại danh sách liên hệ
        } else {
            super.onBackPressed(); // Quay lại hoạt động trước đó
        }
    }
}

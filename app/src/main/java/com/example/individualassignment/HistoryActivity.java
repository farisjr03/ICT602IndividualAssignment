package com.example.individualassignment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> displayedData;
    private ArrayList<String> databaseIds;
    private ArrayAdapter<String> listAdapter;
    private DataHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("Saved Computations");

        dbHelper = new DataHelper(this);
        ListView listView = findViewById(R.id.listView);
        displayedData = new ArrayList<>();
        databaseIds = new ArrayList<>();

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedData);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("RECORD_ID", databaseIds.get(position));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncDataList();
    }

    private void syncDataList() {
        displayedData.clear();
        databaseIds.clear();
        Cursor cursor = dbHelper.getAllData();
        while (cursor.moveToNext()) {
            databaseIds.add(cursor.getString(0));
            String rowItem = "Month: " + cursor.getString(1) + " | Cost: RM " + String.format("%.2f", cursor.getDouble(5));
            displayedData.add(rowItem);
        }
        listAdapter.notifyDataSetChanged();
    }
}
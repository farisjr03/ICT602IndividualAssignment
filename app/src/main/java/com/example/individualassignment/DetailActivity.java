package com.example.individualassignment;

import android.database.Cursor;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

        private EditText etMonth, etUnits, etRebate;
        private TextView tvTotal, tvFinal;
        private DataHelper dbHelper;
        private String recordId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detail);
            setTitle("Modify Entry");

            dbHelper = new DataHelper(this);
            etMonth = findViewById(R.id.etDetailMonth);
            etUnits = findViewById(R.id.etDetailUnits);
            etRebate = findViewById(R.id.etDetailRebate);
            tvTotal = findViewById(R.id.tvDetailTotal);
            tvFinal = findViewById(R.id.tvDetailFinal);
            Button btnUpdate = findViewById(R.id.btnUpdate);
            Button btnDelete = findViewById(R.id.btnDelete);

            recordId = getIntent().getStringExtra("RECORD_ID");
            populateFields();

            btnUpdate.setOnClickListener(v -> {
                String m = etMonth.getText().toString().trim();
                double u = Double.parseDouble(etUnits.getText().toString().trim());
                double r = Double.parseDouble(etRebate.getText().toString().trim());

                double[] calculation = MainActivity.calculateBill(u, r);
                boolean updated = dbHelper.updateData(recordId, m, u, r, calculation[0], calculation[1]);
                if (updated) {
                    Toast.makeText(this, "Record updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (dbHelper.deleteData(recordId) > 0) {
                    Toast.makeText(this, "Record removed", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        private void populateFields() {
            Cursor cursor = dbHelper.getAllData();
            while (cursor.moveToNext()) {
                if (cursor.getString(0).equals(recordId)) {
                    etMonth.setText(cursor.getString(1));
                    etUnits.setText(cursor.getString(2));
                    etRebate.setText(cursor.getString(3));
                    tvTotal.setText(String.format("Total: RM %.2f", cursor.getDouble(4)));
                    tvFinal.setText(String.format("Final Cost: RM %.2f", cursor.getDouble(5)));
                    break;
                }
            }
        }
    }
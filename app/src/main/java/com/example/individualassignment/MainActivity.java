package com.example.individualassignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private SeekBar seekBarUnits;
    private TextView tvUnitsVal;
    private EditText etRebate;
    private TextView tvOutputTotal, tvOutputFinal;
    private DataHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Electricity Bill Calculator");

        dbHelper = new DataHelper(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        seekBarUnits = findViewById(R.id.seekBarUnits);
        tvUnitsVal = findViewById(R.id.tvUnitsVal);
        etRebate = findViewById(R.id.etRebate);
        tvOutputTotal = findViewById(R.id.tvOutputTotal);
        tvOutputFinal = findViewById(R.id.tvOutputFinal);
        Button btnCalculate = findViewById(R.id.btnCalculate);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnAbout = findViewById(R.id.btnAbout);

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        spinnerMonth.setAdapter(adapter);

        seekBarUnits.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int finalVal = Math.max(1, progress); // Enforce min 1 unit
                tvUnitsVal.setText(finalVal + " kWh");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnCalculate.setOnClickListener(v -> handleCalculation(false));
        btnSave.setOnClickListener(v -> handleCalculation(true));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
    }

    private void handleCalculation(boolean saveToDb) {
        double units = Math.max(1, seekBarUnits.getProgress());
        String rebateRaw = etRebate.getText().toString().trim();

        if (rebateRaw.isEmpty()) {
            etRebate.setError("Please supply a rebate percentage");
            return;
        }

        double rebate = Double.parseDouble(rebateRaw);
        if (rebate < 0 || rebate > 5) {
            etRebate.setError("Rebate value must be between 0% and 5%");
            return;
        }

        double[] billMetrics = calculateBill(units, rebate);
        double totalCharges = billMetrics[0];
        double finalCost = billMetrics[1];

        tvOutputTotal.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
        tvOutputFinal.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));

        if (saveToDb) {
            String month = spinnerMonth.getSelectedItem().toString();
            boolean success = dbHelper.insertData(month, units, rebate, totalCharges, finalCost);
            if (success) {
                Toast.makeText(this, "Calculation successfully saved!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static double[] calculateBill(double units, double rebatePercent) {
        double totalCharges = 0;
        double remaining = units;

        // Block 1 (1 - 200 kWh) @ 21.8 sen
        if (remaining > 200) { totalCharges += 200 * 0.218; remaining -= 200; }
        else { totalCharges += remaining * 0.218; remaining = 0; }

        // Block 2 (201 - 300 kWh) @ 33.4 sen
        if (remaining > 100) { totalCharges += 100 * 0.334; remaining -= 100; }
        else { totalCharges += remaining * 0.334; remaining = 0; }

        // Block 3 (301 - 600 kWh) @ 51.6 sen
        if (remaining > 300) { totalCharges += 300 * 0.516; remaining -= 300; }
        else { totalCharges += remaining * 0.516; remaining = 0; }

        // Block 4 (601 - 1000 kWh) @ 54.6 sen
        if (remaining > 0) { totalCharges += remaining * 0.546; }

        double finalCost = totalCharges - (totalCharges * (rebatePercent / 100.0));
        return new double[]{totalCharges, finalCost};
    }
}
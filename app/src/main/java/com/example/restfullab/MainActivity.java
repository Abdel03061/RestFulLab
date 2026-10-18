package com.example.restfullab;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restfullab.api.ApiService;
import com.example.restfullab.api.RetrofitClient;
import com.example.restfullab.model.Report;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private ProgressBar progressBar;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link views
        Button btnGet = findViewById(R.id.btnGet);
        Button btnPost = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);
        tvResult = findViewById(R.id.tvResult);

        // Initialize API service (Singleton)
        apiService = RetrofitClient.getApiService();

        btnGet.setOnClickListener(v -> fetchReportsFromServer());
        btnPost.setOnClickListener(v -> createNewReport());
    }

    // GET request
    private void fetchReportsFromServer() {
        showLoading(true);

        // enqueue() runs the request asynchronously on a background thread
        apiService.getAllReports().enqueue(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Report> reports = response.body();
                    Log.d("API_SUCCESS", "Retrieved " + reports.size() + " records.");
                    Toast.makeText(MainActivity.this,
                            "Successfully loaded " + reports.size() + " items.",
                            Toast.LENGTH_SHORT).show();

                    // Show the first 10 records on screen
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(10, reports.size()); i++) {
                        sb.append("Report ID: ").append(reports.get(i).getId()).append("\n");
                    }
                    tvResult.setText("Total records: " + reports.size()
                            + "\n\nFirst 10:\n" + sb);
                } else {
                    Log.e("API_ERROR", "Server returned HTTP status: " + response.code());
                    Toast.makeText(MainActivity.this,
                            "Server error. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                showLoading(false);
                Log.e("NETWORK_FAILURE", "Connection failed: " + t.getMessage());
                Toast.makeText(MainActivity.this,
                        "Network failure. Please check your internet connection.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // POST request
    private void createNewReport() {
        showLoading(true);

        Report newReport = new Report("Manzanillo", "Fever and cough", "Medium");

        apiService.createReport(newReport).enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    int newId = response.body().getId();
                    Log.d("API_POST", "Report created with ID: " + newId);
                    Toast.makeText(MainActivity.this,
                            "Report created. New ID: " + newId,
                            Toast.LENGTH_LONG).show();
                    tvResult.setText("POST successful.\nCreated report ID: " + newId);
                } else {
                    Log.e("API_ERROR", "POST failed. HTTP status: " + response.code());
                    Toast.makeText(MainActivity.this,
                            "Could not create the report. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Report> call, Throwable t) {
                showLoading(false);
                Log.e("NETWORK_FAILURE", "POST failed: " + t.getMessage());
                Toast.makeText(MainActivity.this,
                        "Network failure. Please check your internet connection.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Show or hide the loading indicator
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
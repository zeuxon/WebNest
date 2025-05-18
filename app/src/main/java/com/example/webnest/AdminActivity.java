package com.example.webnest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminCourseAdapter adapter;
    private List<Course> courseList = new ArrayList<>();
    private List<String> courseIds = new ArrayList<>();
    private FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin felület");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.adminCourseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCourseAdapter(this, courseList, courseIds);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        listenCourses();

        Button addBtn = findViewById(R.id.addCourseButton);
        addBtn.setOnClickListener(v -> showAddDialog());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        selectedImageUri = result.getData().getData();
                    }
                });
    }

    private void listenCourses() {
        db.collection("courses").addSnapshotListener((snapshots, e) -> {
            if (e != null) return;
            courseList.clear();
            courseIds.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Course course = doc.toObject(Course.class);
                    courseList.add(course);
                    courseIds.add(doc.getId());
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_course, null);
        EditText titleEdit = dialogView.findViewById(R.id.editCourseTitle);
        EditText descEdit = dialogView.findViewById(R.id.editCourseDescription);
        EditText priceEditText = dialogView.findViewById(R.id.priceEditText);
        CheckBox paidCheckBox = dialogView.findViewById(R.id.paidCheckBox);
        RadioGroup imageSourceRadioGroup = dialogView.findViewById(R.id.imageSourceRadioGroup);
        RadioButton urlRadioButton = dialogView.findViewById(R.id.urlRadioButton);
        RadioButton galleryRadioButton = dialogView.findViewById(R.id.galleryRadioButton);
        EditText imageUrlEdit = dialogView.findViewById(R.id.editCourseImageUrl);
        Button selectImageButton = dialogView.findViewById(R.id.selectImageButton);
        EditText materialEdit = dialogView.findViewById(R.id.editCourseMaterial);

        urlRadioButton.setChecked(true);
        imageUrlEdit.setVisibility(View.VISIBLE);
        selectImageButton.setVisibility(View.GONE);

        paidCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            priceEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        imageSourceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.urlRadioButton) {
                imageUrlEdit.setVisibility(View.VISIBLE);
                selectImageButton.setVisibility(View.GONE);
            } else {
                imageUrlEdit.setVisibility(View.GONE);
                selectImageButton.setVisibility(View.VISIBLE);
            }
        });

        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        selectedImageUri = null;

        new AlertDialog.Builder(this)
            .setTitle("Új kurzus hozzáadása")
            .setView(dialogView)
            .setPositiveButton("Hozzáadás", (dialog, which) -> {
                String title = titleEdit.getText().toString();
                String desc = descEdit.getText().toString();
                boolean isPaid = paidCheckBox.isChecked();
                int price = 0;
                if (isPaid) {
                    String priceStr = priceEditText.getText().toString();
                    price = priceStr.isEmpty() ? 0 : Integer.parseInt(priceStr);
                }
                String material = materialEdit.getText().toString();
                if (urlRadioButton.isChecked()) {
                    String imageUrl = imageUrlEdit.getText().toString();
                    Course course = new Course(title, desc, imageUrl, isPaid, price, material);
                    db.collection("courses").add(course);
                } else if (galleryRadioButton.isChecked() && selectedImageUri != null) {
                    Course course = new Course(title, desc, selectedImageUri.toString(), isPaid, price, material);
                    db.collection("courses").add(course);
                }
            })
            .setNegativeButton("Mégse", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
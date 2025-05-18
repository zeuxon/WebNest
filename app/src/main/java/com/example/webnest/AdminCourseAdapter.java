package com.example.webnest;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.AdminCourseViewHolder> {

    private List<Course> courseList;
    private List<String> courseIds;
    private Context context;

    public AdminCourseAdapter(Context context, List<Course> courseList, List<String> courseIds) {
        this.context = context;
        this.courseList = courseList;
        this.courseIds = courseIds;
    }

    @NonNull
    @Override
    public AdminCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_list_item_admin, parent, false);
        return new AdminCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.title.setText(course.getTitle());
        holder.description.setText(course.getDescription());

        holder.editButton.setOnClickListener(v -> showEditDialog(position));
        holder.deleteButton.setOnClickListener(v -> deleteCourse(position));
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class AdminCourseViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageButton editButton, deleteButton;

        public AdminCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.adminCourseTitle);
            description = itemView.findViewById(R.id.adminCourseDescription);
            editButton = itemView.findViewById(R.id.editCourseButton);
            deleteButton = itemView.findViewById(R.id.deleteCourseButton);
        }
    }

    private void showEditDialog(int position) {
        Course course = courseList.get(position);
        String courseId = courseIds.get(position);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_course, null);
        EditText titleEdit = dialogView.findViewById(R.id.editCourseTitle);
        EditText descEdit = dialogView.findViewById(R.id.editCourseDescription);
        EditText imageUrlEdit = dialogView.findViewById(R.id.editCourseImageUrl);
        CheckBox paidCheckBox = dialogView.findViewById(R.id.paidCheckBox);
        EditText priceEditText = dialogView.findViewById(R.id.priceEditText);

        paidCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            priceEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        titleEdit.setText(course.getTitle());
        descEdit.setText(course.getDescription());
        imageUrlEdit.setText(course.getImageUrl());
        paidCheckBox.setChecked(course.isPaid());
        if (course.isPaid()) {
            priceEditText.setVisibility(View.VISIBLE);
            priceEditText.setText(String.valueOf(course.getPrice()));
        } else {
            priceEditText.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(context)
            .setTitle("Kurzus szerkesztése")
            .setView(dialogView)
            .setPositiveButton("Mentés", (dialog, which) -> {
                String newTitle = titleEdit.getText().toString();
                String newDesc = descEdit.getText().toString();
                String newImageUrl = imageUrlEdit.getText().toString();
                boolean isPaid = paidCheckBox.isChecked();
                int price = 0;
                if (isPaid) {
                    String priceStr = priceEditText.getText().toString();
                    price = priceStr.isEmpty() ? 0 : Integer.parseInt(priceStr);
                }
                FirebaseFirestore.getInstance().collection("courses").document(courseId)
                    .update("title", newTitle, "description", newDesc, "imageUrl", newImageUrl, "isPaid", isPaid, "price", price)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Sikeres mentés!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Mégse", null)
            .show();
    }

    private void deleteCourse(int position) {
        String courseId = courseIds.get(position);
        FirebaseFirestore.getInstance().collection("courses").document(courseId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Kurzus törölve!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
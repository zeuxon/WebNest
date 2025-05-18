package com.example.webnest;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.title.setText(course.getTitle());
        holder.description.setText(course.getDescription());

        String imageUrl = course.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty() && holder.imageView != null) {
            if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(imageUrl))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
            } else {
                Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
            }
        } else if (holder.imageView != null) {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.price.setVisibility(View.GONE);
        holder.buyButton.setVisibility(View.GONE);
        holder.seeMoreButton.setVisibility(View.GONE);

        if (course.isPaid()) {
            holder.price.setVisibility(View.VISIBLE);
            holder.price.setText(course.getPrice() + " Ft");

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (uid == null) {
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("purchasedCourses").document(course.getTitle())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION ||
                                holder.getAdapterPosition() != position ||
                                !courseList.get(holder.getAdapterPosition()).getTitle().equals(course.getTitle())) {
                            return;
                        }

                        if (documentSnapshot.exists()) {

                            holder.seeMoreButton.setVisibility(View.VISIBLE);
                            holder.buyButton.setVisibility(View.GONE);
                        } else {

                            holder.buyButton.setVisibility(View.VISIBLE);
                            holder.seeMoreButton.setVisibility(View.GONE);

                            holder.buyButton.setOnClickListener(v -> {

                                holder.buyButton.setEnabled(false);
                                holder.buyButton.setText("Feldolgozás...");

                                FirebaseFirestore.getInstance()
                                        .collection("users").document(uid)
                                        .collection("purchasedCourses").document(course.getTitle())
                                        .set(new HashMap<>())
                                        .addOnSuccessListener(aVoid -> {
                                            if (holder.getAdapterPosition() == RecyclerView.NO_POSITION ||
                                                    holder.getAdapterPosition() != position ||
                                                    !courseList.get(holder.getAdapterPosition()).getTitle().equals(course.getTitle())) {
                                                return;
                                            }
                                            holder.buyButton.setVisibility(View.GONE);
                                            holder.seeMoreButton.setVisibility(View.VISIBLE);

                                            Context context = holder.itemView.getContext();

                                            Intent intent = new Intent(context, CourseListActivity.class);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                                    context, 0, intent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                            );

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "course_channel")
                                                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                                                    .setContentTitle("Sikeres vásárlás!")
                                                    .setContentText(course.getTitle() + " kurzust megvásároltad.")
                                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true);

                                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                                        })
                                        .addOnFailureListener(e -> {
                                            if (holder.getAdapterPosition() == RecyclerView.NO_POSITION ||
                                                    holder.getAdapterPosition() != position ||
                                                    !courseList.get(holder.getAdapterPosition()).getTitle().equals(course.getTitle())) {
                                                return;
                                            }
                                            holder.buyButton.setEnabled(true);
                                            holder.buyButton.setText("Megveszem");
                                        });
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION ||
                                holder.getAdapterPosition() != position ||
                                !courseList.get(holder.getAdapterPosition()).getTitle().equals(course.getTitle())) {
                            return;
                        }
                        holder.buyButton.setVisibility(View.GONE);
                        holder.seeMoreButton.setVisibility(View.GONE);
                    });
        } else {
            holder.seeMoreButton.setVisibility(View.VISIBLE);
            holder.buyButton.setVisibility(View.GONE);
            holder.price.setVisibility(View.GONE);
        }

        holder.seeMoreButton.setOnClickListener(v -> {
            String material = course.getMaterial();
            String fileName = course.getTitle() + "_tananyag.txt";
            try {

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(material.getBytes());
                fos.close();
                Toast.makeText(holder.itemView.getContext(), "Tananyag letöltve: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), "Hiba a letöltéskor: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, price;
        ImageView imageView;
        Button buyButton, seeMoreButton;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.courseTitleTextView);
            description = itemView.findViewById(R.id.courseDescriptionTextView);
            price = itemView.findViewById(R.id.coursePriceTextView);
            imageView = itemView.findViewById(R.id.courseImage);
            buyButton = itemView.findViewById(R.id.buyButton);
            seeMoreButton = itemView.findViewById(R.id.seeMoreButton);
        }
    }
}
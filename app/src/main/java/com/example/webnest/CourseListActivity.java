package com.example.webnest;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;

import java.util.ArrayList;
import java.util.List;


public class CourseListActivity extends AppCompatActivity {
    private static final String LOG_TAG = CourseListActivity.class.getName();

    private FirebaseUser user;

    private RecyclerView courseRecyclerView;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;

    private FirebaseFirestore db;

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "course_channel",
                    "Kurzus értesítések",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Értesítések kurzus vásárlásról");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        db = FirebaseFirestore.getInstance();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(this, "Kérlek, engedélyezd a pontos ébresztéseket!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        createNotificationChannel();

        findViewById(R.id.btnResetCourses).setOnClickListener(v -> {
            db.collection("courses")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        courseList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Course course = doc.toObject(Course.class);
                            courseList.add(course);
                        }
                        courseAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Minden kurzus megjelenítve: " + courseList.size(), Toast.LENGTH_SHORT).show();
                    });
        });

        findViewById(R.id.btnPaidCourses).setOnClickListener(v -> {
            db.collection("courses")
                    .whereEqualTo("paid", true)
                    .orderBy("price", com.google.firebase.firestore.Query.Direction.ASCENDING)
                    .limit(5)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        courseList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Course course = doc.toObject(Course.class);
                            courseList.add(course);
                        }
                        courseAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Fizetős kurzusok (max 5): " + courseList.size(), Toast.LENGTH_SHORT).show();
                    });
        });

        findViewById(R.id.btnFreeCoursesPaged).setOnClickListener(v -> {
            db.collection("courses")
                    .whereEqualTo("paid", false)
                    .orderBy("title", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        DocumentSnapshot lastVisible = null;
                        int i = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            if (i == 9) lastVisible = doc;
                            i++;
                        }
                        if (lastVisible != null) {
                            db.collection("courses")
                                    .whereEqualTo("paid", false)
                                    .orderBy("title", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                    .startAfter(lastVisible)
                                    .limit(5)
                                    .get()
                                    .addOnSuccessListener(nextDocs -> {
                                        courseList.clear();
                                        for (DocumentSnapshot doc : nextDocs) {
                                            Course course = doc.toObject(Course.class);
                                            courseList.add(course);
                                        }
                                        courseAdapter.notifyDataSetChanged();
                                        Toast.makeText(this, "Ingyenes kurzusok (10. után 5): " + courseList.size(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            courseList.clear();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                Course course = doc.toObject(Course.class);
                                courseList.add(course);
                            }
                            courseAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Ingyenes kurzusok (max 10): " + courseList.size(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.btnExpensiveCourses).setOnClickListener(v -> {
            db.collection("courses")
                    .whereEqualTo("paid", true)
                    .whereGreaterThan("price", 1000)
                    .orderBy("price", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        courseList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Course course = doc.toObject(Course.class);
                            courseList.add(course);
                        }
                        courseAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Drága fizetős kurzusok (max 3): " + courseList.size(), Toast.LENGTH_SHORT).show();
                    });
        });

        db.collection("courses")
            .whereEqualTo("paid", true)
            .orderBy("price", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Course> paidCourses = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Course course = doc.toObject(Course.class);
                    paidCourses.add(course);
                }
                Log.d(LOG_TAG, "Fizetős kurzusok (max 5, ár szerint növekvő): " + paidCourses.size());
            });

            db.collection("courses")
                    .whereEqualTo("paid", false)
                    .orderBy("title", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        DocumentSnapshot lastVisible = null;
                        int i = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            if (i == 9) lastVisible = doc;
                            i++;
                        }
                        if (lastVisible != null) {
                            db.collection("courses")
                                    .whereEqualTo("paid", false)
                                    .orderBy("title", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                    .startAfter(lastVisible)
                                    .limit(5)
                                    .get()
                                    .addOnSuccessListener(nextDocs -> {
                                        List<Course> nextFreeCourses = new ArrayList<>();
                                        for (DocumentSnapshot doc : nextDocs) {
                                            Course course = doc.toObject(Course.class);
                                            nextFreeCourses.add(course);
                                        }
                                        Log.d(LOG_TAG, "Ingyenes kurzusok (10. után 5, név szerint csökkenő): " + nextFreeCourses.size());
                                    });
                        }
                    });

            db.collection("courses")
                    .whereEqualTo("paid", true)
                    .whereGreaterThan("price", 5000)
                    .orderBy("price", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Course> expensiveCourses = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Course course = doc.toObject(Course.class);
                            expensiveCourses.add(course);
                        }
                        Log.d(LOG_TAG, "Drága fizetős kurzusok (max 3, ár szerint csökkenő): " + expensiveCourses.size());
                    });

        findViewById(R.id.testAlarmButton).setOnClickListener(v -> {
            Intent alarmIntent = new Intent(this, CourseAlarmReceiver.class);
            alarmIntent.putExtra("courseTitle", "Teszt kurzus");
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                    this,
                    12345,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            long triggerAtMillis = System.currentTimeMillis() + 60 * 1000; // IDŐ BEÁLLÍTÁSA
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarmPendingIntent);

            Toast.makeText(this, "Emlékeztető beállítva 1 percre!", Toast.LENGTH_SHORT).show();
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }

        findViewById(R.id.adminIcon).setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    if (isAdmin != null && isAdmin) {
                        startActivity(new Intent(this, AdminActivity.class));
                    } else {
                        Toast.makeText(this, "Ide admin jogosultság kell!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a jogosultság ellenőrzésekor!", Toast.LENGTH_SHORT).show();
                });
        });

        courseRecyclerView = findViewById(R.id.courseRecyclerView);
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(courseList);
        courseRecyclerView.setAdapter(courseAdapter);

        loadCoursesFromFirestore();


    }

    private void loadCoursesFromFirestore() {
        db.collection("courses").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_TAG, "Listen failed.", e);
                    return;
                }
                courseList.clear();
                if (snapshots != null) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Course course = doc.toObject(Course.class);
                        courseList.add(course);
                    }
                }
                courseAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        courseRecyclerView.postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "A bejelentkezés lejárt!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }, 300);
    }



}
package com.example.webnest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {
    private static final String LOG_TAG = CourseListActivity.class.getName();

    private FirebaseUser user;

    private RecyclerView courseRecyclerView;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
        } else {
            Log.d(LOG_TAG, "Not authenticated user!");
            finish();
        }

        courseRecyclerView = findViewById(R.id.courseRecyclerView);
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        courseList = getCourses();
        courseAdapter = new CourseAdapter(courseList);
        courseRecyclerView.setAdapter(courseAdapter);
    }

    private List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("HTML Kurzus", "Tanuld meg a HTML alapjait.", R.drawable.html));
        courses.add(new Course("JavaScript Kurzus", "Sajátítsd el a JavaScriptet webfejlesztéshez.", R.drawable.javascript));
        courses.add(new Course("CSS Kurzus", "Szépítsd weboldalaidat CSS segítségével.", R.drawable.css));
        courses.add(new Course("Java Kurzus", "Ismerd meg a Java programozás alapjait.", R.drawable.java));
        courses.add(new Course("Python Kurzus", "Kezdj el programozni Python nyelven.", R.drawable.python));
        courses.add(new Course("Android Fejlesztés", "Készíts Android alkalmazásokat Java vagy Kotlin nyelven.", R.drawable.android));
        courses.add(new Course("React Kurzus", "Tanuld meg a Reactet modern webalkalmazások készítéséhez.", R.drawable.react));
        courses.add(new Course("Node.js Kurzus", "Fejlessz backend alkalmazásokat Node.js segítségével.", R.drawable.nodejs));
        courses.add(new Course("SQL Kurzus", "Kezeld az adatbázisokat SQL segítségével.", R.drawable.sql));
        courses.add(new Course("Git Kurzus", "Verziókezelés a projektjeidhez Git segítségével.", R.drawable.git));
        return courses;
    }

}
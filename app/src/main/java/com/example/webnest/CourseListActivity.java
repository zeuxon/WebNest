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
        courses.add(new Course("HTML Course", "Learn the basics of HTML.", R.drawable.html));
        courses.add(new Course("JavaScript Course", "Master JavaScript for web development.", R.drawable.javascript));
        courses.add(new Course("CSS Course", "Style your websites with CSS.", R.drawable.css));
        courses.add(new Course("Java Course", "Understand the fundamentals of Java programming.", R.drawable.java));
        courses.add(new Course("Python Course", "Get started with Python programming.", R.drawable.python));
        courses.add(new Course("Android Development", "Build Android apps using Java or Kotlin.", R.drawable.android));
        courses.add(new Course("React Course", "Learn React for building modern web apps.", R.drawable.react));
        courses.add(new Course("Node.js Course", "Develop backend applications with Node.js.", R.drawable.nodejs));
        courses.add(new Course("SQL Course", "Manage databases with SQL.", R.drawable.sql));
        courses.add(new Course("Git Course", "Version control your projects with Git.", R.drawable.git));
        return courses;
    }

}
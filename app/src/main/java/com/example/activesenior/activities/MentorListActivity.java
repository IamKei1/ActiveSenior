// activities/MentorListActivity.java
package com.example.activesenior.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.adapters.MentorAdapter;
import com.example.activesenior.models.Mentor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MentorListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MentorAdapter mentorAdapter;
    private List<Mentor> mentorList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_list);

        recyclerView = findViewById(R.id.mentorRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mentorAdapter = new MentorAdapter(mentorList);
        recyclerView.setAdapter(mentorAdapter);

        loadMentors();
    }

    private void loadMentors() {
        db.collection("users")
                .whereEqualTo("role", "mentor") // role이 mentor인 사람만
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mentorList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Mentor mentor = doc.toObject(Mentor.class);
                        mentorList.add(mentor);
                    }
                    mentorAdapter.notifyDataSetChanged();
                });
    }
}

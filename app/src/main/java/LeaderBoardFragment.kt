package com.quizapp.tork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quizapp.tork.adapter.LeaderBoardAdapter
import com.quizapp.tork.model.User

class LeaderBoardFragment : Fragment() {

    private lateinit var adapter: LeaderBoardAdapter
    private val userList = ArrayList<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leader_board, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rec_leader)

        adapter = LeaderBoardAdapter(userList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        loadLeaderboardData()

        return view
    }

    private fun loadLeaderboardData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }
}
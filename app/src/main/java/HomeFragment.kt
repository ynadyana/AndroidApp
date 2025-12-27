package com.quizapp.tork

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quizapp.tork.adapter.CategoryAdapter
import com.quizapp.tork.databinding.FragmentHomeBinding
import com.quizapp.tork.model.Category

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CategoryAdapter
    private val allCategories = ArrayList<Category>() // Holds all data
    private val filteredCategories = ArrayList<Category>() // Holds filtered data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize Firestore and FirebaseAuth
        val database = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser?.displayName

        // Set user name
        //binding.uname.text = user ?: "User"
        Log.i("HomeFragment", "Logged in user: $user")

        // Initialize adapter
        adapter = CategoryAdapter(filteredCategories)
        binding.catItems.layoutManager = GridLayoutManager(context, 2)
        binding.catItems.adapter = adapter

        // Fetch categories from Firestore
        database.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeFragment", "Error fetching categories: ", error)
                    return@addSnapshotListener
                }

                allCategories.clear()
                snapshot?.documents?.forEach { document ->
                    val category = document.toObject(Category::class.java)
                    category?.cat_id = document.id // Set document ID
                    if (category != null) {
                        allCategories.add(category)
                    }
                }

                // Initially display all categories
                filteredCategories.clear()
                filteredCategories.addAll(allCategories)
                adapter.notifyDataSetChanged()
            }

        // Add search functionality
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCategories(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun filterCategories(query: String) {
        filteredCategories.clear()
        if (query.isEmpty()) {
            filteredCategories.addAll(allCategories) // Show all if query is empty
        } else {
            val searchQuery = query.lowercase()
            filteredCategories.addAll(
                allCategories.filter { category ->
                    category.cat_title?.lowercase()?.contains(searchQuery) == true
                }
            )
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

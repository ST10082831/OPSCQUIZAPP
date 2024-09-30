package com.example.opscquizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.opscquizapp.api.ApiClient
import com.example.opscquizapp.api.Category
import com.example.opscquizapp.api.CategoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategorySelectionActivity : AppCompatActivity() {

    private lateinit var categoryListView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var difficultySpinner: Spinner
    private lateinit var startQuizButton: Button

    private var categories: List<Category> = emptyList()
    private var selectedCategory: Category? = null
    private var selectedDifficulty: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)

        categoryListView = findViewById(R.id.categoryListView)
        progressBar = findViewById(R.id.categoryProgressBar)
        difficultySpinner = findViewById(R.id.difficultySpinner)
        startQuizButton = findViewById(R.id.startQuizButton)

        setupDifficultySpinner()
        fetchCategories()

        startQuizButton.setOnClickListener {
            if (selectedCategory == null) {
                Toast.makeText(this, getString(R.string.select_category_prompt), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("category_id", selectedCategory!!.id)
            intent.putExtra("category_name", selectedCategory!!.name)
            intent.putExtra("difficulty", selectedDifficulty)
            startActivity(intent)
        }
    }

    private fun setupDifficultySpinner() {
        val difficulties = arrayOf(
            getString(R.string.any_difficulty),
            getString(R.string.easy),
            getString(R.string.medium),
            getString(R.string.hard)
        )
        val difficultyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            difficulties
        )
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        difficultySpinner.adapter = difficultyAdapter

        difficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedDifficulty = when (position) {
                    0 -> null // Any Difficulty
                    1 -> "easy"
                    2 -> "medium"
                    3 -> "hard"
                    else -> null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDifficulty = null
            }
        }
    }

    private fun fetchCategories() {
        progressBar.visibility = View.VISIBLE

        val call = ApiClient.apiService.getCategories()
        call.enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(
                call: Call<CategoryResponse>,
                response: Response<CategoryResponse>
            ) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    categories = response.body()?.trivia_categories ?: emptyList()
                    displayCategories()
                } else {
                    showError(getString(R.string.failed_to_load_categories))
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showError(getString(R.string.error_fetching_categories, t.localizedMessage))
            }
        })
    }

    private fun displayCategories() {
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_single_choice,
            categoryNames
        )
        categoryListView.adapter = adapter
        categoryListView.choiceMode = ListView.CHOICE_MODE_SINGLE

        startQuizButton.isEnabled = false // Disable the button first

        categoryListView.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
            startQuizButton.isEnabled = true // Enable the button once a category is selected
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // to handle language settings
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("QuizAppSettings", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }
}

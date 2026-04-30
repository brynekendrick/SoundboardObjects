package com.example.soundboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev.DeveloperActivity
import com.example.mobdev.R
import com.example.mobdev.RegisterActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.util.*

class SoundboardActivity : AppCompatActivity() {

    private lateinit var soundPlayer: SoundPlayer
    private lateinit var soundAdapter: SoundAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private var soundsList: MutableList<SoundItem> = mutableListOf()
    private var filteredSoundsList: MutableList<SoundItem> = mutableListOf()
    private var currentCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soundboard)

        soundPlayer = SoundPlayer.getInstance(this)

        setupUI()
        loadSounds()
        setupListeners()

        val developer = findViewById<ImageView>(R.id.btn_dev)

        developer.setOnClickListener {
            Toast.makeText(this, "We are the developers!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, DeveloperActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupUI() {
        recyclerView = findViewById(R.id.recyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        searchView = findViewById(R.id.searchView)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        soundAdapter = SoundAdapter(this, filteredSoundsList)
        recyclerView.adapter = soundAdapter

        setupCategories()
        setupBottomNavigation()
    }

    private fun setupCategories() {
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Animals"))
        tabLayout.addTab(tabLayout.newTab().setText("Cartoon"))
        tabLayout.addTab(tabLayout.newTab().setText("Nature"))
        tabLayout.addTab(tabLayout.newTab().setText("Effects"))
        tabLayout.addTab(tabLayout.newTab().setText("Music"))
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true // Already here
                R.id.nav_favorites -> {
                    filterByFavorites()
                    true
                }
                R.id.nav_profile -> {
                    try {
                        val intent = Intent(this, Class.forName("com.example.soundboard.ProfileActivity"))
                        startActivity(intent)
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error opening profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                R.id.nav_settings -> {
                    try {
                        val intent = Intent(this, Class.forName("com.example.soundboard.Activity"))
                        startActivity(intent)
                        true
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                else -> false
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            Toast.makeText(this, "Add new sound feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentCategory = tab.text.toString()
                filterSounds()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false
            override fun onQueryTextChange(newText: String): Boolean {
                filterSounds(newText)
                return true
            }
        })

        soundAdapter.setOnSoundItemClickListener(object : SoundAdapter.OnSoundItemClickListener {
            override fun onPlayButtonClick(soundItem: SoundItem, position: Int) {
                playSoundItem(soundItem)
            }

            override fun onFavoriteButtonClick(soundItem: SoundItem, position: Int) {
                soundItem.toggleFavorite()
                soundAdapter.updateItem(position)
            }
        })

        soundPlayer.setOnSoundCompletionListener {
            soundAdapter.notifyDataSetChanged()
        }
    }

    private fun loadSounds() {
        soundsList.add(SoundItem("1", "Dog Bark", "Animals", "0:03", R.raw.dog_bark))
        soundsList.add(SoundItem("2", "Cat Meow", "Animals", "0:02", R.raw.cat_meow))
        soundsList.add(SoundItem("3", "Thunder", "Nature", "0:05", R.raw.thunder))
        soundsList.add(SoundItem("4", "Laughing", "Cartoon", "0:04", R.raw.laugh))
        soundsList.add(SoundItem("5", "Applause", "Effects", "0:06", R.raw.applause))
        soundsList.add(SoundItem("6", "Electric Guitar", "Music", "0:03", R.raw.electric_guitar))
        soundsList.add(SoundItem("7", "Monster", "Cartoon", "0:02", R.raw.monster))
        soundsList.add(SoundItem("8", "Harmonica", "Music", "0:05", R.raw.harmonica))
        soundsList.add(SoundItem("9", "Lyre", "Cartoon", "0:04", R.raw.harp))
        soundsList.add(SoundItem("10", "Typing", "Effects", "0:06", R.raw.typing))
        soundsList.add(SoundItem("11", "Gong", "Music", "0:04", R.raw.gong))
        soundsList.add(SoundItem("12", "Beeping", "Effects", "0:04", R.raw.beep))
        soundsList.add(SoundItem("13", "Explosion", "Nature", "0:06", R.raw.explode))
        soundsList.add(SoundItem("14", "Flowing Water", "Nature", "0:04", R.raw.flow))
        soundsList.add(SoundItem("15", "Birds Chirping", "Animals", "0:04", R.raw.bird_chirp))



        soundsList.forEach {
            if (it.isResourceSound()) {
                soundPlayer.loadSound(this, it.id, it.resourceId)
            }
        }

        filterSounds()
    }

    private fun filterSounds(query: String = "") {
        val searchText = query.lowercase(Locale.getDefault())
        filteredSoundsList.clear()
        soundsList.forEach {
            val matchesCategory = currentCategory == "All" || it.category == currentCategory
            val matchesSearch = it.title.lowercase(Locale.getDefault()).contains(searchText)
            if (matchesCategory && matchesSearch) filteredSoundsList.add(it)
        }
        soundAdapter.updateSoundsList(filteredSoundsList)
    }

    private fun filterByFavorites() {
        filteredSoundsList.clear()
        filteredSoundsList.addAll(soundsList.filter { it.isFavorite })
        soundAdapter.updateSoundsList(filteredSoundsList)
    }

    private fun playSoundItem(soundItem: SoundItem) {
        if (soundPlayer.isPlaying() && soundPlayer.getCurrentSoundName() == soundItem.id) {
            soundPlayer.stopSound()
        } else {
            if (soundItem.duration.startsWith("0:0") && soundItem.duration.length <= 4) {
                soundPlayer.playShortSound(soundItem.id)
            } else {
                soundPlayer.playLongSound(this, soundItem.resourceId, soundItem.id)
            }
        }
        soundAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
    }
}

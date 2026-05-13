package com.example.soundboard

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev.DeveloperActivity
import com.example.mobdev.R
import com.example.mobdev.UserSession
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.util.Locale
import java.util.concurrent.TimeUnit

class SoundboardActivity : AppCompatActivity() {

    private lateinit var soundPlayer: SoundPlayer
    private lateinit var soundAdapter: SoundAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var bottomNavigation: BottomNavigationView
    private var soundsList: MutableList<SoundItem> = mutableListOf()
    private var filteredSoundsList: MutableList<SoundItem> = mutableListOf()
    private var currentCategory: String = "All"

    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val uri = result.data?.data ?: return@registerForActivityResult
        result.data?.let { data ->
            val takeFlags = data.flags and
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            try {
                if (takeFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0) {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            } catch (_: SecurityException) {
                // Some providers do not support persistable permission; playback may still work this session.
            }
        }
        appendImportedSound(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soundboard)

        soundPlayer = SoundPlayer.getInstance(this)

        setupUI()
        loadSounds()
        setupListeners()

        if (intent.getBooleanExtra(EXTRA_OPEN_FAVORITES, false)) {
            bottomNavigation.selectedItemId = R.id.nav_favorites
            filterByFavorites()
        }

        findViewById<ImageView>(R.id.btn_dev).setOnClickListener {
            Toast.makeText(this, "We are the developers!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, DeveloperActivity::class.java))
        }
    }

    private fun setupUI() {
        recyclerView = findViewById(R.id.recyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        searchView = findViewById(R.id.searchView)
        bottomNavigation = findViewById(R.id.bottom_navigation)

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
        tabLayout.addTab(tabLayout.newTab().setText("Imports"))
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    tabLayout.getTabAt(0)?.select()
                    currentCategory = "All"
                    searchView.setQuery("", false)
                    filterSounds()
                    true
                }
                R.id.nav_favorites -> {
                    filterByFavorites()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            showAddSoundOptions()
        }
    }

    private fun showAddSoundOptions() {
        val options = arrayOf("Attach audio from device", "Browse free sounds online")
        AlertDialog.Builder(this)
            .setTitle("Add sound")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchAudioPicker()
                    1 -> startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://freesound.org/browse/")
                        )
                    )
                }
            }
            .show()
    }

    private fun launchAudioPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        pickAudioLauncher.launch(intent)
    }

    private fun appendImportedSound(uri: Uri) {
        val title = queryDisplayName(uri) ?: "Imported sound"
        val durationLabel = formatDurationMs(metadataDurationMs(uri))
        UserSession.appendCustomSound(this, title, uri.toString())
        val entries = UserSession.loadCustomSoundEntries(this)
        val last = entries.lastOrNull() ?: return
        soundsList.add(SoundItem(last.id, last.title, "Imports", durationLabel, last.uriString))
        filterSounds(searchView.query?.toString().orEmpty())
        Toast.makeText(this, "Sound added to your soundboard", Toast.LENGTH_SHORT).show()
    }

    private fun queryDisplayName(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
        }
    }

    private fun metadataDurationMs(uri: Uri): Long {
        val r = MediaMetadataRetriever()
        return try {
            r.setDataSource(this, uri)
            r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            try {
                r.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun formatDurationMs(ms: Long): String {
        if (ms <= 0L) return "~0:00"
        val s = TimeUnit.MILLISECONDS.toSeconds(ms)
        val m = s / 60
        val rem = s % 60
        return String.format(Locale.getDefault(), "%d:%02d", m, rem)
    }

    private fun setupListeners() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentCategory = tab.text.toString()
                filterSounds(searchView.query?.toString().orEmpty())
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

        UserSession.loadCustomSoundEntries(this).forEach { e ->
            val uri = Uri.parse(e.uriString)
            val dur = formatDurationMs(metadataDurationMs(uri))
            soundsList.add(SoundItem(e.id, e.title, "Imports", dur, e.uriString))
        }

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
        when {
            !soundItem.filePath.isNullOrBlank() -> {
                val uri = Uri.parse(soundItem.filePath)
                if (soundPlayer.isPlaying() && soundPlayer.getCurrentSoundName() == soundItem.id) {
                    soundPlayer.stopSound()
                } else {
                    soundPlayer.playLongSoundFromUri(this, uri, soundItem.id)
                }
            }
            soundItem.isResourceSound() -> {
                if (soundPlayer.isPlaying() && soundPlayer.getCurrentSoundName() == soundItem.id) {
                    soundPlayer.stopSound()
                } else {
                    if (soundItem.duration.startsWith("0:0") && soundItem.duration.length <= 4) {
                        soundPlayer.playShortSound(soundItem.id)
                    } else {
                        soundPlayer.playLongSound(this, soundItem.resourceId, soundItem.id)
                    }
                }
            }
        }
        soundAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
    }

    companion object {
        const val EXTRA_OPEN_FAVORITES = "extra_open_favorites"
    }
}

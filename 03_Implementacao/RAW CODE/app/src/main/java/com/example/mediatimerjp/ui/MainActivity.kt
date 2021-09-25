package com.example.mediatimerjp.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.example.mediatimerjp.R
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val wrapper = TimerWrapper.getInstance()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wrapper.context = applicationContext
        wrapper.loadTheme(this)
        val sharedPreference = getSharedPreferences("MediaTimerSaveUser", Context.MODE_PRIVATE)
        val uID = sharedPreference.getString("uID" ,"")
        val editor = sharedPreference.edit()
        if (uID.equals("")){
            if (editor != null) {
                editor.putString("uID", Database.getUserId())
                editor.apply()
            }
        }
        wrapper.currentModel = viewModel
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.themes -> {
                if(wrapper.isOnMain){
                    val navController = Navigation.findNavController(this,R.id.nav_host_fragment)
                    navController.navigate(R.id.action_mainFragment_to_ChangeThemeFragment)
                }
                else {
                    val navController = Navigation.findNavController(this,R.id.nav_host_fragment)
                    navController.navigate(R.id.action_groupFragment_to_ChangeThemeFragment)
                }
            }
            R.id.logout -> {
                logout()
            }
        }
        return true
    }


    private fun logout() {
        val sharedPreference = getSharedPreferences("MediaTimerSaveUser", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.remove("uID")
        editor.apply()
        FirebaseAuth.getInstance().signOut()
        saveTimers()
        startActivity(Intent(this, LogInActivity::class.java))
    }

    private fun saveTimers(){
        wrapper.saveTimersToSharedPreference(this, "SaveFile")
    }

    override fun onStop() {
        super.onStop()
        saveTimers()
    }


}
package com.dahham.movieprogresskeeper

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    var authentication: Authentication? = null
    var firestore: Firestore? = null
    val RC_SIGN_IN = 14

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val dialog = MovieEditDialog()
            dialog.show(supportFragmentManager, null)
        }

        if (authentication == null){
            authentication = Authentication.getInstance();
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (pref.getBoolean("sync", false) && authentication?.isInitialize() == true){
            initSync(authentication?.getUser()?.id!!)
        } else {
            initAuth()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.about -> {

                val dialog = BottomSheetDialog(this)
                dialog.setContentView(R.layout.about_layout)
                dialog.show()

                true
            }

            R.id.sync -> {
                pref = PreferenceManager.getDefaultSharedPreferences(this)
                pref?.edit().putBoolean("sync", pref.getBoolean("sync",false).not()).commit()
                true
            }

            R.id.start_sync -> {
                initAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val value = pref.getBoolean("sync", false)

        menu?.findItem(R.id.sync)?.isChecked = value
        menu?.findItem(R.id.start_sync)?.isEnabled = value

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            authentication?.handleIntent(data ?: return){
                user, errorMessage ->

                if (errorMessage != null){
                    Toast.makeText(this, "Error occurred, try later", Toast.LENGTH_LONG).show()
                    return@handleIntent
                }

                initSync(user?.id!!)

                return@handleIntent
            }
        }
    }

    private fun initAuth(){

        if (authentication?.isInitialize() == false){
            startActivityForResult(authentication?.getIntent(this), RC_SIGN_IN)
            return;
        }

    }

    private fun initSync(userId: String){
        val database = MovieDatabase.database(this)
        firestore = Firestore.getInstance(userId)

        val progressDialog = ProgressDialog.show(this, "Syncing", "Please Wait", true,true);
        progressDialog.show()
        val job = GlobalScope.launch {
            val movies = database.getAllMovies().toMutableList()


            firestore?.syncDatabase(movies){
                old, new ->
                database.update(*old.toTypedArray())
                database.put(*new.toTypedArray())

                progressDialog.dismiss()
            }

        }
    }

    override fun onDestroy() {
        authentication?.signOut()
        super.onDestroy()
    }
}


package com.dahham.movieprogresskeeper

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.util.Base64Utils
import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    private var database: MovieDao? = null
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

        if (authentication == null) {
            authentication = Authentication.getInstance();
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        val userId = pref.getString(getString(R.string.userId), null)
        if (userId != null) {
            initSync(Base64Utils.decode(userId).toString(Charset.defaultCharset()))
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
                val syncOn =  pref.getBoolean("sync", false)
                pref.edit().putBoolean("sync", syncOn.not()).commit()

                if (syncOn) {
                    pref.edit().remove(getString(R.string.userId)).apply()
                    authentication?.signOut()
                }else {
                    initAuth()
                }

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

        if (requestCode == RC_SIGN_IN) {
            authentication?.handleIntent(data ?: return) { user, errorMessage ->

                if (errorMessage != null) {
                    Toast.makeText(this, "Error occurred, try later", Toast.LENGTH_LONG).show()
                    return@handleIntent
                }

                pref.edit().putString(
                    getString(R.string.userId),
                    Base64Utils.encode(user?.id?.toByteArray(Charset.defaultCharset()))
                ).apply()
                initSync(user?.id!!)

                return@handleIntent
            }
        }
    }

    private fun initAuth() {

        val userId = pref.getString(getString(R.string.userId), null)
        if (userId == null) {
            startActivityForResult(authentication?.getIntent(this), RC_SIGN_IN)
            return;
        } else {
            initSync(Base64Utils.decode(userId)!!.toString(Charset.defaultCharset()))
        }

    }

    private fun initSync(userId: String) {
        database = MovieDatabase.database(this)
        firestore = Firestore.getInstance(userId)

        val progressDialog = ProgressDialog.show(this, "Syncing", "Please Wait", true, true);
        progressDialog.show()
        val job = GlobalScope.launch {
            val movies = database?.getAllMovies()?.toMutableList() ?: return@launch


            firestore?.syncDatabase(movies) { old, new ->
                database?.update(*old.toTypedArray())
                database?.put(*new.toTypedArray())

                progressDialog.dismiss()
            }

        }
    }

    override fun onDestroy() {
        GlobalScope.launch {
            firestore?.updateDatabase(database?.getAllMovies()?.toMutableList() ?: return@launch)
        }

        super.onDestroy()
    }
}


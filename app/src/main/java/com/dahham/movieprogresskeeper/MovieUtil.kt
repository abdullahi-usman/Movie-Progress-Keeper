package com.dahham.movieprogresskeeper

import android.content.Context
import android.preference.PreferenceManager
import com.google.android.gms.common.util.Base64Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.charset.Charset

fun updateCloudDatabase(database: MovieDao, context: Context) {

    val userId = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.userId), null)

    if (userId != null) {
        GlobalScope.launch(Dispatchers.IO) {

            val firestore = Firestore.getInstance(
                    Base64Utils.decode(userId).toString(
                            Charset.defaultCharset()
                    )
            )

            firestore.updateDatabase(database.getAllMovies().toMutableList())

        }
    }

}
package com.dahham.movieprogresskeeper

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.TypeVariable

class Firestore private constructor(val id: String) {

    private lateinit var firebaseFirestore: FirebaseFirestore


    suspend fun syncDatabase(movies:  MutableList<Movie>, callback: ((movies: MutableList<Movie>) -> Unit)?) {

        withContext(Dispatchers.IO) {

            val firebaseFirestoreCollections = firebaseFirestore.collection("users")
            val firebaseFirestoreDocument = firebaseFirestoreCollections.document(id)
            firebaseFirestoreDocument.get().addOnCompleteListener { task ->


                if (task.result != null && task.result?.data != null && task.result?.data?.isEmpty() != true) {

                    val result = task.result
                    val data = result?.data?.getValue("records") as? List<HashMap<String, Any>>

                    for (_movie in data!!){
                        val movie = Movie(name = _movie["name"].toString(), season = (_movie["season"] as Number).toInt(), episode = (_movie["episode"] as Number).toInt())
                        if (movies.contains(movie)){
                            val index = movies.indexOf(movie)
                            val _m = movies.elementAt(index)
                            if (movie.season > _m.season || movie.episode > _m.episode){
                                _m.season = movie.season
                                _m.episode = movie.episode
                            }
                        } else {
                            movies.add(movie)
                        }
                    }


                }
                firebaseFirestoreDocument.set(mapOf(Pair("records", movies)))


                callback?.invoke(movies)

            }
        }
    }

    companion object {

        var firestore: Firestore? = null
        fun getInstance(id: String): Firestore {
            if (firestore == null) {
                firestore = Firestore(id);
            }

            if (firestore!!::firebaseFirestore.isInitialized.not()) {
                firestore!!.firebaseFirestore = FirebaseFirestore.getInstance();
            }

            return firestore!!
        }
    }
}
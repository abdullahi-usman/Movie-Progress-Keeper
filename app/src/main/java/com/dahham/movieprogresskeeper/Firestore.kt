package com.dahham.movieprogresskeeper

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.lang.reflect.TypeVariable

class Firestore private constructor(val id: String) {

    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseFirestoreCollections = firebaseFirestore.collection("users")
    private val firebaseFirestoreDocument = firebaseFirestoreCollections.document(id)


    suspend fun syncDatabase(movies:  MutableList<Movie>, callback: ((updated_movies: MutableList<Movie>, new_movies: MutableList<Movie>) -> Unit)?) {

        withContext(Dispatchers.IO) {


            firebaseFirestoreDocument.get().addOnCompleteListener { task ->


                val newMovies =  mutableListOf<Movie>()
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
                            newMovies.add(movie)
                        }
                    }


                }
                firebaseFirestoreDocument.set(mapOf(Pair("records", mutableListOf<Movie>(*movies.toTypedArray(), *newMovies.toTypedArray()))))

                callback?.invoke(movies, newMovies)

            }
        }
    }

    companion object {

        var firestore: Firestore? = null
        fun getInstance(id: String): Firestore {
            if (firestore == null) {
                firestore = Firestore(id);
            }

            return firestore!!
        }
    }
}
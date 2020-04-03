package com.dahham.movieprogresskeeper

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.movie_edit.*

class MovieEditDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.movie_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        cancel_button.setOnClickListener {
            dismiss()
        }

        val movie = arguments?.get("movie") as? Movie

        if (movie != null){
            movie_text.setText(movie.name)
            movie_text.isEnabled = false

            seasons_picker.value = movie.season
            episodes_picker.value = movie.episode
        }

        save_button.setOnClickListener {
            val movieNmae = movie_text.text.toString()

            if (movieNmae.isEmpty()){
                Toast.makeText(context,"Movie name canno be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val seasons = seasons_picker.value
            val episode = episodes_picker.value

            movie?.season = seasons
            movie?.episode = episode

            if (movie != null){
                MovieDatabase.database(it.context).update(movie)
            }else {
                try {
                    MovieDatabase.database(it.context).put(Movie(name = movieNmae, season = seasons, episode = episode))
                }catch (e: SQLiteConstraintException){
                    Toast.makeText(context, "Name already in list", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

            }

            dismiss()
        }

    }
}
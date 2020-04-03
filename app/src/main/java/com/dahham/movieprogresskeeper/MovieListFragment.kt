package com.dahham.movieprogresskeeper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_movie.*
import kotlinx.android.synthetic.main.movie_detail.view.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MovieListFragment : Fragment() {
    
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.findViewById<Button>(R.id.button_first).setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
        movies_list.layoutManager = LinearLayoutManager(context)

        MovieDatabase.database(context!!).getAll().observe(this.viewLifecycleOwner,  Observer {
            if (it == null || it.isEmpty()){
                if (!(movie_list_switcher.currentView is CardView)){
                    movie_list_switcher.showNext()
                }

                return@Observer
            }

            if (!(movie_list_switcher.currentView is RecyclerView)){
                movie_list_switcher.showNext()
            }

            if (movies_list.adapter == null){
                movies_list.adapter = MovieListAdapter(it)
            } else {
                (movies_list.adapter as MovieListAdapter).movies = it
            }


            movies_list.adapter!!.notifyDataSetChanged()
        })

    }

    inner class MovieListAdapter(var movies: List<Movie>): RecyclerView.Adapter<MovieListViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieListViewHolder {
            return MovieListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.movie_detail, parent, false));
        }

        override fun getItemCount(): Int {
            return movies.count()
        }

        override fun onBindViewHolder(holder: MovieListViewHolder, position: Int) {
            holder.bind(movies.get(position))
        }

    }

    inner class MovieListViewHolder(val view: View): RecyclerView.ViewHolder(view){

        lateinit var movie: Movie
        init {
            view.setOnLongClickListener {view ->


                val database = MovieDatabase.database(view.context)

                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menu.add("Delete").setOnMenuItemClickListener { menuItem ->
                    popupMenu.dismiss()
                    database.delete(movie)
                    return@setOnMenuItemClickListener true
                }
                popupMenu.menu.add("Edit").setOnMenuItemClickListener {
                    popupMenu.dismiss()
                    val dialog = MovieEditDialog()
                    dialog.arguments = Bundle()
                    dialog.arguments?.putParcelable("movie", movie)

                    dialog.show(childFragmentManager, null)
                    return@setOnMenuItemClickListener true
                }

                popupMenu.show()
                return@setOnLongClickListener true
            }
        }

        fun bind(movie: Movie){
            this.movie = movie
            view.movie_name_ext.text = movie.name
            view.season_number.text = movie.season.toString()
            view.episode_number.text = movie.episode.toString()
        }
    }


}
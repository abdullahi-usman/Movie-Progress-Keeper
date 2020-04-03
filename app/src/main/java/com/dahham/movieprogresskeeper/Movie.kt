package com.dahham.movieprogresskeeper

import android.os.Parcel
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable


@Entity
data class Movie(@PrimaryKey(autoGenerate = true) val id: Int = 0,  var name: String, var season: Int, var episode: Int): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(season)
        parcel.writeInt(episode)
    }

    override fun describeContents(): Int {
        return 0
    }


    override fun equals(other: Any?): Boolean {
        if (!(other is Movie))return false

        return name == other.name && season == other.season && episode == other.episode
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<Movie> {
        override fun createFromParcel(parcel: Parcel): Movie {
            return Movie(parcel)
        }

        override fun newArray(size: Int): Array<Movie?> {
            return arrayOfNulls(size)
        }
    }

}

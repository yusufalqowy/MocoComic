package yu.desk.mococomic.utils

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import yu.desk.mococomic.R

enum class FilterStatus(
    val title: String,
    val path: String,
    @IdRes val id: Int = View.generateViewId(),
) {
    All("All", ""),
    Favorite("Favorite", ""),
    Ongoing("Ongoing", "Ongoing"),
    Completed("Completed", "Completed"),
    ;

    companion object {
        fun getById(
            @IdRes id: Int,
        ): FilterStatus = FilterStatus.entries.find { it.id == id } ?: All
    }
}

enum class FilterOrder(
    val title: String,
    val path: String,
    @IdRes val id: Int = View.generateViewId(),
) {
    LastUpdate("Last Updated", "update"),
    LastAdded("Last Added", "latest"),
    Popular("Popular", "popular"),
    ASC("A-Z", "title"),
    DESC("Z-A", "titlereverse"),
    ;

    companion object {
        fun getById(
            @IdRes id: Int,
        ): FilterOrder = FilterOrder.entries.find { it.id == id } ?: LastUpdate
    }
}

enum class FilterGenre(
    val title: String,
    val path: String,
    @IdRes val id: Int = View.generateViewId(),
) {
    Action("Action", "9"),
    Adventure("Adventure", "12"),
    Comedy("Comedy", "4"),
    Drama("Drama", "15"),
    Game("Game", "19"),
    Historical("Historical", "113"),
    Horror("Horror", "24"),
    Isekai("Isekai", "135"),
    Mature("Mature", "2205,58"),
    Magic("Magic", "10"),
    MartialArt("Martial Arts", "57"),
    Medical("Medical", "2851"),
    Military("Military", "59"),
    Mystery("Mystery", "23"),
    Reincarnation("Reincarnation", "2443"),
    Romance("Romance", "14"),
    School("School", "21"),
    Science("Science Fiction", "20"),
    SliceOfLife("Slice of Life", "65"),
    Sport("Sport", "22"),
    Thriller("Thriller", "213"),
    ;

    companion object {
        fun getById(
            @IdRes id: Int,
        ): FilterGenre? = FilterGenre.entries.find { it.id == id }
    }
}

enum class FilterType(
    val title: String,
    val path: String,
    @IdRes val id: Int = View.generateViewId(),
) {
    All("All", ""),
    Manga("Manga", "Manga"),
    Manhwa("Manhwa", "Manhwa"),
    Manhua("Manhua", "Manhua"),
    Comic("Comic", "Comic"),
    ;

    companion object {
        fun getById(
            @IdRes id: Int,
        ): FilterType = FilterType.entries.find { it.id == id } ?: All
    }
}

enum class ComicType(
    val title: String,
    @DrawableRes val icon: Int,
) {
    Manga("Manga", R.drawable.ic_japan),
    Manhwa("Manhwa", R.drawable.ic_south_korea),
    Manhua("Manhua", R.drawable.ic_china),
    Comic("Comic", R.drawable.ic_usa),
    Unknown("-", 0),
}

enum class FilterComic(
    val path: String,
    @DrawableRes val icon: Int,
) {
    Refresh("refresh", R.drawable.ic_replay),
    Latest("latest", R.drawable.ic_replay),
    Trending("trending", R.drawable.ic_replay),
    Rating("rating", R.drawable.ic_replay),
    Views("views", R.drawable.ic_replay),
    News("new", R.drawable.ic_replay),
    Alphabet("az", R.drawable.ic_replay),
    Project("project", R.drawable.ic_replay),
    Mirror("mirror", R.drawable.ic_replay),
}
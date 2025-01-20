package yu.desk.mococomic.domain.model

data class ComicDetail(
    val author: String = "",
    val chapters: List<Chapter> = listOf(),
    val cover: String = "",
    val genres: List<String> = listOf(),
    val published: String = "",
    val score: String = "",
    val serialization: String = "",
    val status: String = "",
    val subtitle: String = "",
    val synopsis: String = "",
    val title: String = "",
)
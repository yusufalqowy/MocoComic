package yu.desk.mococomic.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import yu.desk.mococomic.databinding.ItemChapterBinding
import yu.desk.mococomic.domain.model.Chapter

class ListChapterAdapter : RecyclerView.Adapter<ListChapterAdapter.ViewHolder>() {

    private var listChapter: List<Chapter> = emptyList()
    private var selectedChapter: Chapter? = null
    private var onChapterClickListener: ((Chapter) -> Unit)? = null


    class ViewHolder(val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.binding, listChapter[position])
    }

    private fun bind(binding: ItemChapterBinding, chapter: Chapter) {
        binding.apply {
            itemChapter.isItemSelected = chapter.slug == selectedChapter?.slug
            itemChapter.isTrailingIconVisible = chapter.isAlreadyRead
            itemChapter.label = chapter.chapter
            onChapterClickListener?.let { listener ->
                root.setOnClickListener {
                    listener.invoke(chapter)
                }
            }

        }
    }

    override fun getItemCount(): Int = listChapter.size

    fun setSelectedChapter(chapter: Chapter) {
        selectedChapter = listChapter.find { it.slug == chapter.slug }
        selectedChapter?.let {
            val position = listChapter.indexOf(selectedChapter)
            notifyItemChanged(position)
        }
    }

    fun setItem(list: List<Chapter>) {
        listChapter = list
    }

    fun setOnChapterClickListener(listener: (Chapter) -> Unit) {
        onChapterClickListener = listener
    }
}

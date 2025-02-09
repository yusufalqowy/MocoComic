package yu.desk.mococomic.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import yu.desk.mococomic.databinding.ItemChapterHistoryBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.FirebaseChapter
import yu.desk.mococomic.utils.getTimeAgo

class ChapterHistoryPagingDataAdapter : PagingDataAdapter<FirebaseChapter, ChapterHistoryPagingDataAdapter.ViewHolder>(diffCallback) {
	private var onItemClickListener: ((Comic) -> Unit)? = null

	companion object {
		val diffCallback =
			object : DiffUtil.ItemCallback<FirebaseChapter>() {
				override fun areContentsTheSame(
					oldItem: FirebaseChapter,
					newItem: FirebaseChapter,
				): Boolean = oldItem.slug == newItem.slug

				override fun areItemsTheSame(
					oldItem: FirebaseChapter,
					newItem: FirebaseChapter,
				): Boolean = oldItem == newItem
			}
	}

	class ViewHolder(
		val binding: ItemChapterHistoryBinding,
	) : RecyclerView.ViewHolder(binding.root)

	override fun onBindViewHolder(
		holder: ViewHolder,
		position: Int,
	) {
		val item = getItem(position)
		item?.let {
			onBind(holder.binding, it)
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): ViewHolder {
		val binding = ItemChapterHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return ViewHolder(binding)
	}

	private fun onBind(
		binding: ItemChapterHistoryBinding,
		item: FirebaseChapter,
	) {
		binding.apply {
			itemChapter.title = item.comic?.title ?: "-"
			itemChapter.subtitle = item.chapter + "\n" + item.createdAt.getTimeAgo()
			itemChapter.setLeadingImageUrl(item.comic?.cover ?: "")
			onItemClickListener?.let { click ->
				binding.root.setOnClickListener {
					item.comic?.let {
						click.invoke(it.toComic())
					}
				}
			}
		}
	}

	fun onItemClickListener(listener: (Comic) -> Unit) {
		onItemClickListener = listener
	}
}
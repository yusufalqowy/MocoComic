package yu.desk.mococomic.presentation.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.ItemChapterHistoryBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.FirebaseChapter
import yu.desk.mococomic.utils.getTimeAgo
import yu.desk.mococomic.utils.setVisible

class ChapterHistoryPagingDataAdapter : PagingDataAdapter<FirebaseChapter, ChapterHistoryPagingDataAdapter.ViewHolder>(diffCallback) {
	private var onItemClickListener: ((Comic) -> Unit)? = null
	private var onRemoveItemClickListener: ((FirebaseChapter) -> Unit)? = null
	private val deletedItems = mutableListOf<FirebaseChapter>()

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

	override fun getItemViewType(position: Int): Int {
		return when {
			deletedItems.contains(getItem(position)) -> 2
			else -> 1
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): ViewHolder {
		val binding = ItemChapterHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		val viewHolder = ViewHolder(binding)
		if (viewType == 2) {
			viewHolder.itemView.apply {
				setVisible(false)
				layoutParams = RecyclerView.LayoutParams(0, 0)
			}
		}
		return viewHolder
	}

	private fun onBind(
		binding: ItemChapterHistoryBinding,
		item: FirebaseChapter,
	) {
		binding.apply {
			itemChapter.title = item.comic?.title ?: "-"
			itemChapter.subtitle = item.chapter + "\n" + item.createdAt.getTimeAgo()
			itemChapter.setLeadingImageUrl(item.comic?.cover ?: "")
			itemChapter.setOnLongClickListener {
				val popupMenu = PopupMenu(it.context, it, Gravity.END)
				val menuItem = popupMenu.menu.add(it.context.getString(R.string.text_remove_item))
				menuItem.setOnMenuItemClickListener {
					onRemoveItemClickListener?.invoke(item)
					return@setOnMenuItemClickListener true
				}
				popupMenu.show()
				return@setOnLongClickListener true
			}
			onItemClickListener?.let { click ->
				binding.root.setOnClickListener {
					item.comic?.let {
						click.invoke(it.toComic())
					}
				}
			}
		}
	}

	fun removeItem(chapter: FirebaseChapter) {
		val listItems = snapshot().toMutableList().also { it.removeAll(deletedItems) }
		val item = listItems.find { it?.slug == chapter.slug }
		val position = listItems.indexOf(item)
		notifyItemRemoved(position)
		deletedItems.add(chapter)
	}

	fun onItemClickListener(listener: (Comic) -> Unit) {
		onItemClickListener = listener
	}

	fun onRemoveItemClickListener(listener: (FirebaseChapter) -> Unit) {
		onRemoveItemClickListener = listener
	}
}
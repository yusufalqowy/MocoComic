package yu.desk.mococomic.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import yu.desk.mococomic.domain.model.Comic

class ComicDiffCallback(
	private val oldList: List<Any>,
	private val newList: List<Any>,
) : DiffUtil.Callback() {
	override fun getOldListSize(): Int = oldList.size

	override fun getNewListSize(): Int = newList.size

	override fun areItemsTheSame(
		oldItemPosition: Int,
		newItemPosition: Int,
	): Boolean {
		val oldItem = oldList[oldItemPosition]
		val newItem = newList[newItemPosition]
		return if (oldItem is Comic && newItem is Comic) oldItem.slug == newItem.slug else oldItem == newItem
	}

	override fun areContentsTheSame(
		oldItemPosition: Int,
		newItemPosition: Int,
	): Boolean {
		val oldItem = oldList[oldItemPosition]
		val newItem = newList[newItemPosition]
		return oldItem == newItem
	}
}
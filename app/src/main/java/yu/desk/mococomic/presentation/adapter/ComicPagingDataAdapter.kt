package yu.desk.mococomic.presentation.adapter

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.ItemComicBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.utils.loadImage
import yu.desk.mococomic.utils.setVisible

class ComicPagingDataAdapter : PagingDataAdapter<Comic, ComicPagingDataAdapter.ViewHolder>(diffCallback) {
	private var onItemClickListener: ((Comic) -> Unit)? = null
	private var onMenuDetailClickListener: ((Comic) -> Unit)? = null
	private var onMenuRemoveClickListener: ((Comic) -> Unit)? = null

	companion object {
		val diffCallback =
			object : DiffUtil.ItemCallback<Comic>() {
				override fun areContentsTheSame(
					oldItem: Comic,
					newItem: Comic,
				): Boolean = oldItem.slug == newItem.slug

				override fun areItemsTheSame(
					oldItem: Comic,
					newItem: Comic,
				): Boolean = oldItem == newItem
			}
	}

	class ViewHolder(
		val binding: ItemComicBinding,
	) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
		override fun onCreateContextMenu(menu: ContextMenu?, p1: View?, p2: ContextMenu.ContextMenuInfo?) {
			menu?.setHeaderTitle("Menu")
			menu?.add(0, binding.btnMenu.id, 0, "Detail")
			menu?.add(0, binding.btnMenu.id, 0, "Delete")
		}
	}

	override fun onBindViewHolder(
		holder: ViewHolder,
		position: Int,
	) {
		val item = getItem(position)
		if (item != null) {
			onBind(holder.binding, item)
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): ViewHolder {
		val binding = ItemComicBinding.inflate(LayoutInflater.from(parent.context), parent, false)

		return ViewHolder(binding)
	}

	private fun onBind(
		binding: ItemComicBinding,
		item: Comic,
	) {
		binding.apply {
			ivCover.loadImage(item.cover)
			tvName.text = item.title
			tvChapter.setVisible(item.lastChapter != null)
			tvChapter.text = item.lastChapter?.title.toString()
			val icon = item.type.icon
			if (icon != 0) {
				ivType.setImageResource(icon)
			} else {
				ivType.setVisible(false)
			}
			btnMenu.setVisible(true)
			ivFavorite.setVisible(false)

			btnMenu.setOnClickListener {
				val popupMenu = PopupMenu(it.context, it)
				popupMenu.inflate(R.menu.comic_menu)
				popupMenu.setForceShowIcon(true)
				popupMenu.setOnMenuItemClickListener { menuItem ->
					when (menuItem.itemId) {
						R.id.menuDetail -> {
							onMenuDetailClickListener?.invoke(item)
						}

						R.id.menuDelete -> {
							onMenuRemoveClickListener?.invoke(item)
						}
					}
					return@setOnMenuItemClickListener true
				}
				popupMenu.show()
			}

			onItemClickListener?.let { click ->
				binding.root.setOnClickListener {
					click.invoke(item)
				}
			}
		}
	}

	fun removeItem(comic: Comic) {
		val item = snapshot().items.find { it.slug == comic.slug }
		val position = snapshot().items.indexOf(item)
		notifyItemRemoved(position)
	}

	fun onItemClickListener(listener: (Comic) -> Unit) {
		onItemClickListener = listener
		onMenuDetailClickListener = listener
	}

	fun onMenuRemoveClickListener(listener: (Comic) -> Unit) {
		onMenuRemoveClickListener = listener
	}
}
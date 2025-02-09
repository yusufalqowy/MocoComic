package yu.desk.mococomic.presentation.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.ItemComicBinding
import yu.desk.mococomic.databinding.ItemComicPagingBinding
import yu.desk.mococomic.databinding.ItemComicShimmerBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.utils.AuthHelper
import yu.desk.mococomic.utils.PagingType
import yu.desk.mococomic.utils.loadImage
import yu.desk.mococomic.utils.setInvisible
import yu.desk.mococomic.utils.setVisible

class ComicAdapter(
	private var visibleItem: Float = 0f,
	private val showFavoriteIcon: Boolean = false,
	private val showBlockMenu: Boolean = false,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	companion object {
		private const val TYPE_COMIC = 0
		private const val TYPE_PAGING = 1
		private const val TYPE_SHIMMER = 2
	}

	private var listItems: MutableList<Any> = mutableListOf()
	private var onItemClickListener: ((Comic) -> Unit)? = null
	private var onLoadMoreClickListener: (() -> Unit)? = null
	private var onBlockComicClickListener: ((Comic) -> Unit)? = null

	class ComicViewHolder(
		val binding: ItemComicBinding,
	) : RecyclerView.ViewHolder(binding.root)

	class ShimmerViewHolder(
		val binding: ItemComicShimmerBinding,
	) : RecyclerView.ViewHolder(binding.root)

	class PagingViewHolder(
		val binding: ItemComicPagingBinding,
	) : RecyclerView.ViewHolder(binding.root)

	override fun getItemViewType(position: Int): Int {
		val item = listItems[position]
		return if (item is Comic) {
			if (item.paging != null) {
				TYPE_PAGING
			} else {
				TYPE_COMIC
			}
		} else {
			TYPE_SHIMMER
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): RecyclerView.ViewHolder {
		val binding =
			ItemComicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		val shimmerBinding = ItemComicShimmerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		val pagingBinding = ItemComicPagingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		if (visibleItem > 0f) {
			val margin = parent.context.resources.getDimensionPixelSize(R.dimen.dp_4)
			val itemWidth = (parent.context.resources.displayMetrics.widthPixels).div(visibleItem).minus(margin).toInt()
			binding.root.updateLayoutParams {
				width = itemWidth
			}
			shimmerBinding.root.updateLayoutParams {
				width = itemWidth
			}
		}

		return when (viewType) {
			TYPE_COMIC -> ComicViewHolder(binding)
			TYPE_PAGING -> PagingViewHolder(pagingBinding)
			else -> ShimmerViewHolder(shimmerBinding)
		}
	}

	override fun onBindViewHolder(
		holder: RecyclerView.ViewHolder,
		position: Int,
	) {
		val item = listItems[position]
		if (holder is ComicViewHolder && item is Comic) {
			onBind(holder.binding, item)
		} else if (holder is PagingViewHolder && item is Comic) {
			onBindPaging(holder.binding, item, position)
		}
	}

	private fun onBindPaging(
		binding: ItemComicPagingBinding,
		item: Comic,
		position: Int,
	) {
		binding.apply {
			item.paging?.let {
				btnLoadMore.setInvisible(it == PagingType.Loading)
				loadingProgress.setVisible(it == PagingType.Loading)
				if (it == PagingType.Error) {
					btnLoadMore.text = binding.root.context.getString(R.string.text_retry)
				} else {
					btnLoadMore.text = binding.root.context.getString(R.string.text_load_more)
				}
				onLoadMoreClickListener?.let { listener ->
					btnLoadMore.setOnClickListener { _ ->
						listener.invoke()
						listItems[position] = item.copy(paging = PagingType.Loading)
						notifyItemChanged(position)
					}
				}
			}
		}
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
			ivFavorite.setVisible(showFavoriteIcon && Firebase.auth.currentUser != null)
			val favoriteIcon = if (item.isFavorite) R.drawable.ic_favorite_fill else R.drawable.ic_favorite_outline
			ivFavorite.setImageResource(favoriteIcon)
			val icon = item.type.icon
			if (icon != 0) {
				ivType.setImageResource(icon)
			} else {
				ivType.setVisible(false)
			}
			btnMenu.setVisible(false)

			if (showBlockMenu && AuthHelper.isUserLogin()) {
				binding.root.setOnLongClickListener {
					val popupMenu = PopupMenu(it.context, ivCover, Gravity.TOP)
					val menuItem = popupMenu.menu.add(it.context.getString(R.string.text_block_comic))
					menuItem.setOnMenuItemClickListener {
						onBlockComicClickListener?.invoke(item)
						return@setOnMenuItemClickListener true
					}
					popupMenu.show()
					true
				}
			}

			onItemClickListener?.let { click ->
				binding.root.setOnClickListener {
					click.invoke(item)
				}
			}
		}
	}

	override fun getItemCount(): Int = listItems.size

	fun setOnLoadMoreClickListener(listener: () -> Unit) {
		onLoadMoreClickListener = listener
	}

	fun setVisibleItem(visibleItem: Float) {
		this.visibleItem = visibleItem
	}

	fun removeItem(comic: Comic) {
		val item = listItems.find { if (it is Comic) it.slug == comic.slug else false }
		val index = listItems.indexOf(item)
		if (index >= 0) {
			listItems.removeAt(index)
			notifyItemRemoved(index)
		}
	}

	fun changeItem(comic: Comic) {
		val item = listItems.find { if (it is Comic) it.slug == comic.slug else false }
		val index = listItems.indexOf(item)
		if (index >= 0) {
			listItems[index] = comic
			notifyItemChanged(index)
		}
	}

	fun setOnItemClickListener(listener: (Comic) -> Unit) {
		onItemClickListener = listener
	}

	fun setOnBlockComicClickListener(listener: (Comic) -> Unit) {
		onBlockComicClickListener = listener
	}

	fun setItems(items: List<Any>) {
		val diffResult = DiffUtil.calculateDiff(ComicDiffCallback(listItems, items))
		listItems = items.toMutableList()
		diffResult.dispatchUpdatesTo(this)
	}
}
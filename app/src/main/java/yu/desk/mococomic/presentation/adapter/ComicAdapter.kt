package yu.desk.mococomic.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.ItemComicBinding
import yu.desk.mococomic.databinding.ItemComicShimmerBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.utils.ComicType
import yu.desk.mococomic.utils.loadImage
import yu.desk.mococomic.utils.setVisible

class ComicAdapter(
    private var visibleItem: Float = 0f,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val COMIC_TYPE = 1
        private const val COMIC_SHIMMER_TYPE = 2
    }

    private var listItems: List<Any> = emptyList()
    private var onItemClickListener: ((Comic) -> Unit)? = null

    class ComicViewHolder(
        val binding: ItemComicBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    class ShimmerViewHolder(
        val binding: ItemComicShimmerBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int = if (listItems[position] is Comic) COMIC_TYPE else COMIC_SHIMMER_TYPE

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemComicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val shimmerBinding = ItemComicShimmerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

        return if (viewType == COMIC_TYPE) ComicViewHolder(binding) else ShimmerViewHolder(shimmerBinding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = listItems[position]
        if (holder is ComicViewHolder && item is Comic) {
            onBind(holder.binding, item)
        }
    }

    private fun onBind(
        binding: ItemComicBinding,
        item: Comic,
    ) {
        binding.apply {
            ivCover.loadImage(item.cover)
            tvName.text = item.title
            tvChapter.text = item.lastChapter.chapter
            val icon = ComicType.valueOf(item.type.replaceFirstChar { it.uppercase() }).icon
            if (icon != 0) {
                ivType.setImageResource(icon)
            } else {
                ivType.setVisible(false)
            }
            btnMenu.setVisible(false)

            onItemClickListener?.let { click ->
                binding.root.setOnClickListener {
                    click.invoke(item)
                }
            }
        }
    }

    override fun getItemCount(): Int = listItems.size

    fun setVisibleItem(visibleItem: Float) {
        this.visibleItem = visibleItem
    }

    fun setOnItemClickListener(listener: (Comic) -> Unit) {
        onItemClickListener = listener
    }

    fun setItems(items: List<Any>) {
        val diffResult = DiffUtil.calculateDiff(ComicDiffCallback(listItems, items))
        listItems = items
        diffResult.dispatchUpdatesTo(this)
    }
}
package yu.desk.mococomic.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import yu.desk.mococomic.databinding.ItemCarouselBinding
import yu.desk.mococomic.databinding.ItemCarouselShimmerBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.utils.loadImage

class CarouselComicAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val COMIC_TYPE = 1
        private const val COMIC_SHIMMER_TYPE = 2
    }

    private var items: List<Any> = emptyList()
    private var onComicClick: ((Comic) -> Unit)? = null

    class ComicViewHolder(
        val binding: ItemCarouselBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    class ShimmerViewHolder(
        val binding: ItemCarouselShimmerBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int = if (items[position] is Comic) COMIC_TYPE else COMIC_SHIMMER_TYPE

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val shimmerBinding = ItemCarouselShimmerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == COMIC_TYPE) ComicViewHolder(binding) else ShimmerViewHolder(shimmerBinding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = items[position]
        if (holder is ComicViewHolder && item is Comic) {
            bind(holder.binding, item)
        }
    }

    private fun bind(
        binding: ItemCarouselBinding,
        item: Comic,
    ) {
        binding.apply {
            ivCover.loadImage(item.cover)
            ivCoverBackground.loadImage(item.cover, false)
            tvName.text = item.title
            tvChapter.text = item.lastChapter.chapter
            onComicClick?.let {
                root.setOnClickListener { it(item) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setOnItemClickListener(listener: (Comic) -> Unit) {
        onComicClick = listener
    }

    fun setItems(items: List<Any>) {
        val diffResult = DiffUtil.calculateDiff(ComicDiffCallback(oldList = this.items, newList = items))
        this.items = items
        diffResult.dispatchUpdatesTo(this)
    }
}
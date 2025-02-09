package yu.desk.mococomic.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.asDrawable
import coil3.load
import coil3.request.allowHardware
import coil3.util.DebugLogger
import coil3.util.Logger
import yu.desk.mococomic.databinding.ItemImageChapterBinding
import yu.desk.mococomic.utils.setInvisible
import yu.desk.mococomic.utils.setVisible

class ListImageChapterAdapter : RecyclerView.Adapter<ListImageChapterAdapter.ViewHolder>() {
	private var onItemClickListener: ((String) -> Unit)? = null
	private val diffCallback =
		object : DiffUtil.ItemCallback<String>() {
			override fun areContentsTheSame(
				oldItem: String,
				newItem: String,
			): Boolean = oldItem == newItem

			override fun areItemsTheSame(
				oldItem: String,
				newItem: String,
			): Boolean = oldItem == newItem
		}
	private val listItem = AsyncListDiffer(this, diffCallback)
	private var recyclerView: RecyclerView? = null

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		this.recyclerView = recyclerView
	}

	class ViewHolder(
		val binding: ItemImageChapterBinding,
	) : RecyclerView.ViewHolder(binding.root)

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): ViewHolder {
		val binding = ItemImageChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(
		holder: ViewHolder,
		position: Int,
	) {
		bind(holder.binding, listItem.currentList[position], position)
	}

	@SuppressLint("SetTextI18n")
	private fun bind(
		binding: ItemImageChapterBinding,
		item: String,
		position: Int,
	) {
		binding.apply {
			// Update View Here
			onItemClickListener?.let { click ->
				binding.root.setOnClickListener {
					click.invoke(item)
				}
			}

			tvLoadingImage.text = "Loading Image ${position + 1}/${listItem.currentList.size}"
			tvErrorImage.text = "Failed Load Image ${position + 1}/${listItem.currentList.size}"
			val imageLoader = ImageLoader.Builder(ivImageItem.context).logger(DebugLogger(Logger.Level.Warn)).build()
			ivImageItem.load(data = item, imageLoader = imageLoader) {
				allowHardware(false)
				listener(
					onStart = {
						layoutLoading.setVisible(true)
						layoutErrorImage.setVisible(false)
						ivImageItem.setInvisible(true)
					},
					onError = { req, res ->
						layoutLoading.setVisible(false)
						layoutErrorImage.setVisible(true)
						ivImageItem.setInvisible(true)
						btnRetryImage.setOnClickListener {
							imageLoader.enqueue(req)
						}
					},
					onSuccess = { req, res ->
						layoutLoading.setVisible(false)
						layoutErrorImage.setVisible(false)
						ivImageItem.setVisible(true)
						ivImageItem.setImageDrawable(res.image.asDrawable(root.resources))
					},
				)
			}
		}
	}

	override fun getItemCount(): Int = listItem.currentList.size

	fun setItem(list: List<String>) {
		listItem.submitList(list)
	}

	fun setOnItemClickListener(listener: (Any) -> Unit) {
		onItemClickListener = listener
	}
}
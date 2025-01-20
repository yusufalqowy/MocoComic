package yu.desk.mococomic.presentation.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.asDrawable
import coil3.load
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.result
import coil3.util.DebugLogger
import coil3.util.Logger
import yu.desk.mococomic.databinding.ItemImageChapterBinding
import yu.desk.mococomic.utils.setInvisible
import yu.desk.mococomic.utils.setVisible

class ListImageChapterAdapter : RecyclerView.Adapter<ListImageChapterAdapter.ViewHolder>(){
    private lateinit var listItem: List<Any>
    private var onItemClickListener: ((Any) -> Unit)? = null

    class ViewHolder(val binding: ItemImageChapterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.binding, listItem[position], position)
    }

    @SuppressLint("SetTextI18n")
    private fun bind(binding: ItemImageChapterBinding, item: Any, position: Int) {
        binding.apply {
            /*Update View Here*/
            onItemClickListener?.let { click ->
                binding.root.setOnClickListener{
                    click.invoke(item)
                }
            }

            tvLoadingImage.text = "Loading Image ${position + 1}/${listItem.size}"
            tvErrorImage.text = "Failed Load Image ${position + 1}/${listItem.size}"
            val loader = ImageLoader.Builder(ivImageItem.context).build()

            var imageLoader = ImageLoader.Builder(ivImageItem.context).logger(DebugLogger(Logger.Level.Error)).build()

            ivImageItem.load(item.toString(), imageLoader = imageLoader){
                memoryCacheKey(item.toString())
                allowHardware(false)
                crossfade(true)
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
                    }
                )
            }


            /*ivImageItem.load(item.toString(), imageLoader = loader){
                memoryCacheKey(item.toString())
                allowHardware(false)
                crossfade(true)
                listener(
                    onStart = {
                        layoutLoading.setVisible(true)
                        layoutErrorImage.setVisible(false)
                        ivImageItem.setVisible(false)
                    },
                    onError = { req, res ->
                        layoutLoading.setVisible(false)
                        layoutErrorImage.setVisible(true)
                        ivImageItem.setVisible(false)
                        btnRetryImage.setOnClickListener {
//                            loader.enqueue(req)
                        }
                    },
                    onSuccess = { req, res ->
                        layoutLoading.setVisible(false)
                        layoutErrorImage.setVisible(false)
                        ivImageItem.setVisible(true)
                    }
                )
            }*/
        }
    }

    override fun getItemCount(): Int = listItem.size

    fun setItem(list: List<Any>){
        listItem = list
    }

    fun setOnItemClickListener(listener: (Any) -> Unit) {
        onItemClickListener = listener
    }
}

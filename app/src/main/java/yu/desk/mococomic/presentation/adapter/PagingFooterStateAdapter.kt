package yu.desk.mococomic.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.ItemComicPagingBinding
import yu.desk.mococomic.utils.setInvisible
import yu.desk.mococomic.utils.setVisible

class PagingFooterStateAdapter : LoadStateAdapter<PagingFooterStateAdapter.ViewHolder>() {
	class ViewHolder(
		val binding: ItemComicPagingBinding,
	) : RecyclerView.ViewHolder(binding.root)

	override fun onBindViewHolder(
		holder: ViewHolder,
		loadState: LoadState,
	) {
		onBInd(holder.binding, loadState)
	}

	private fun onBInd(
		binding: ItemComicPagingBinding,
		loadState: LoadState,
	) {
		binding.apply {
			loadingProgress.setVisible(loadState is LoadState.Loading)
			btnLoadMore.text = root.context.getString(R.string.text_retry)
			btnLoadMore.setInvisible(loadState !is LoadState.Error)
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		loadState: LoadState,
	): ViewHolder {
		val binding = ItemComicPagingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return ViewHolder(binding)
	}
}
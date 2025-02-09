package yu.desk.mococomic.presentation.component

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.LayoutComicFilterSideSheetBinding
import yu.desk.mococomic.presentation.adapter.TabFilterAdapter
import yu.desk.mococomic.utils.FilterGenre
import yu.desk.mococomic.utils.FilterOrder
import yu.desk.mococomic.utils.FilterStatus
import yu.desk.mococomic.utils.FilterType

class ComicFilterSideSheet(
	context: Context,
	private val fragmentManager: FragmentManager,
) : SideSheetDialog(context, R.style.Theme_MocoComic_SideSheetDialog) {
	private lateinit var binding: LayoutComicFilterSideSheetBinding
	private var filterListener: ((FilterStatus, FilterType, FilterOrder, List<FilterGenre>) -> Unit)? = null
	private var tabFilterAdapter: TabFilterAdapter? = null
	private var tabFilter = TabFilter()
	private var tabGenre = TabGenre()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = LayoutComicFilterSideSheetBinding.inflate(layoutInflater)
		val layoutParams =
			ViewGroup.LayoutParams(
				context.resources.displayMetrics.widthPixels
					.div(2),
				ViewGroup.LayoutParams.MATCH_PARENT,
			)
		initView()
		setContentView(binding.root, layoutParams)
	}

	override fun show() {
		super.show()
		if (this::binding.isInitialized) {
			binding.viewPager.adapter = tabFilterAdapter
		}
	}

	fun showSideSheet(
		filterStatus: FilterStatus,
		filterType: FilterType,
		filterOrder: FilterOrder,
		filterGenres: List<FilterGenre>,
		applyListener: (FilterStatus, FilterType, FilterOrder, List<FilterGenre>) -> Unit,
	) {
		if (!this.isShowing) {
			tabFilter =
				TabFilter().also {
					it.setFilter(filterStatus, filterType, filterOrder)
				}
			tabGenre =
				TabGenre().also {
					it.filterGenres = filterGenres
				}
			tabFilterAdapter =
				TabFilterAdapter(fragmentManager, lifecycle).apply {
					setItems(listOf(tabFilter, tabGenre))
				}
			show()
			filterListener = applyListener
		}
	}

	private fun initView() {
		binding.apply {
			// Handle for can drag bottom sheet
			viewPager.getChildAt(0).isNestedScrollingEnabled = false
			// Make all page active
			viewPager.offscreenPageLimit = 2

			tabFilterAdapter =
				TabFilterAdapter(fragmentManager, lifecycle).apply {
					setItems(listOf(tabFilter, tabGenre))
				}
			viewPager.adapter = tabFilterAdapter

			TabLayoutMediator(tabLayout, viewPager) { tab, position ->
				tab.text =
					when (position) {
						0 -> "Filter"
						else -> "Genre"
					}
			}.attach()

			btnApplyFilter.setOnClickListener {
				Log.e(
					"APPLY",
					"""
					${tabFilter.filterStatus}
					${tabFilter.filterType}
					${tabFilter.filterOrder}
					${tabGenre.filterGenres}
					""".trimIndent(),
				)
				filterListener?.invoke(
					tabFilter.filterStatus,
					tabFilter.filterType,
					tabFilter.filterOrder,
					tabGenre.filterGenres,
				)
				dismiss()
			}
		}
	}
}
package yu.desk.mococomic.presentation.component

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import yu.desk.mococomic.databinding.LayoutComicFilterBottomSheetBinding
import yu.desk.mococomic.presentation.adapter.TabFilterAdapter
import yu.desk.mococomic.utils.FilterGenre
import yu.desk.mococomic.utils.FilterOrder
import yu.desk.mococomic.utils.FilterStatus
import yu.desk.mococomic.utils.FilterType

class ComicFilterBottomSheet(context: Context, private val fragmentManager: FragmentManager) : BottomSheetDialog(context) {
    private lateinit var binding: LayoutComicFilterBottomSheetBinding
    private var filterListener: ((FilterStatus, FilterType, FilterOrder, List<FilterGenre>) -> Unit)? = null
    private var tabFilter = TabFilter()
    private var tabGenre = TabGenre()
    private var tabFilterAdapter: TabFilterAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutComicFilterBottomSheetBinding.inflate(layoutInflater)
        initView()
        setContentView(binding.root)
    }

    override fun show() {
        super.show()
        if(this::binding.isInitialized){
            binding.viewPager.adapter = tabFilterAdapter
        }
    }

    private fun initView() {
        binding.apply {
            //Handle for can drag bottom sheet
            viewPager.getChildAt(0).isNestedScrollingEnabled = false
            //Make all page active
            viewPager.offscreenPageLimit = 2
            //Handle force close "Fragment no longer exists" when open bottom sheet again
            viewPager.isSaveEnabled = false
            tabFilterAdapter = TabFilterAdapter(fragmentManager, lifecycle).apply {
                setItems(listOf(tabFilter, tabGenre))
            }
            viewPager.adapter = tabFilterAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Filter"
                    else -> "Genre"
                }
            }.attach()

            btnApply.setOnClickListener {
                Log.e("APPLY","""
                    ${tabFilter.filterStatus}
                    ${tabFilter.filterType}
                    ${tabFilter.filterOrder}
                    ${tabGenre.filterGenres}
                """.trimIndent())
                filterListener?.invoke(tabFilter.filterStatus, tabFilter.filterType, tabFilter.filterOrder, tabGenre.filterGenres)
                dismiss()
            }
        }
    }

    fun showBottomSheet(filterStatus: FilterStatus, filterType: FilterType, filterOrder: FilterOrder, filterGenres: List<FilterGenre>, applyListener: (FilterStatus, FilterType, FilterOrder, List<FilterGenre>) -> Unit) {
        tabFilter = TabFilter().also {
            it.setFilter(filterStatus, filterType, filterOrder)
        }
        tabGenre = TabGenre().also {
            it.filterGenres = filterGenres
        }
        tabFilterAdapter = TabFilterAdapter(fragmentManager, lifecycle).apply {
            setItems(listOf(tabFilter, tabGenre))
        }
        if(!isShowing){
            show()
            filterListener = applyListener
        }
    }

    companion object {
        const val TAG = "ComicFilterBottomSheet"
    }
}

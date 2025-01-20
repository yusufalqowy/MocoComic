package yu.desk.mococomic.presentation.component

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import yu.desk.mococomic.databinding.FragmentTabFilterBinding
import yu.desk.mococomic.utils.FilterOrder
import yu.desk.mococomic.utils.FilterStatus
import yu.desk.mococomic.utils.FilterType

class TabFilter : Fragment() {
    private lateinit var binding: FragmentTabFilterBinding
    var filterStatus: FilterStatus = FilterStatus.All
    var filterType: FilterType = FilterType.All
    var filterOrder: FilterOrder = FilterOrder.LastUpdate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTabFilterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        updateFilter()
        Log.e("FILTER", "RESUME")
    }

    private fun updateFilter() {
        if (this::binding.isInitialized) {
            binding.apply {
                cgStatus.check(filterStatus.id)
                cgType.check(filterType.id)
                cgOrder.check(filterOrder.id)
            }
        }
    }

    fun setFilter(
        filterStatus: FilterStatus,
        filterType: FilterType,
        filterOrder: FilterOrder,
    ) {
        this.filterStatus = filterStatus
        this.filterType = filterType
        this.filterOrder = filterOrder
    }

    private fun initListener() {
        binding.apply {
            cgStatus.setOnCheckedStateChangeListener { _, checkedIds ->
                filterStatus = FilterStatus.getById(checkedIds.first())
            }

            cgOrder.setOnCheckedStateChangeListener { _, checkedIds ->
                filterOrder = FilterOrder.getById(checkedIds.first())
            }

            cgType.setOnCheckedStateChangeListener { _, checkedIds ->
                filterType = FilterType.getById(checkedIds.first())
            }
        }
    }

    private fun initView() {
        binding.apply {
            FilterStatus.entries.forEach {
                val chip =
                    Chip(requireContext(), null).apply {
                        val drawable =
                            ChipDrawable.createFromAttributes(
                                requireContext(),
                                null,
                                0,
                                com.google.android.material.R.style.Widget_Material3_Chip_Filter
                            )
                        setChipDrawable(drawable)
                        text = it.title
                        id = it.id
                        isChecked = it == filterStatus
                    }
                cgStatus.addView(chip)
            }

            FilterType.entries.forEach {
                val chip =
                    Chip(requireContext(), null).apply {
                        val drawable =
                            ChipDrawable.createFromAttributes(
                                requireContext(),
                                null,
                                0,
                                com.google.android.material.R.style.Widget_Material3_Chip_Filter
                            )
                        setChipDrawable(drawable)
                        text = it.title
                        id = it.id
                        isChecked = it == filterType
                    }
                cgType.addView(chip)
            }

            FilterOrder.entries.forEach {
                val chip =
                    Chip(requireContext(), null).apply {
                        val drawable =
                            ChipDrawable.createFromAttributes(
                                requireContext(),
                                null,
                                0,
                                com.google.android.material.R.style.Widget_Material3_Chip_Filter
                            )
                        setChipDrawable(drawable)
                        text = it.title
                        id = it.id
                        isChecked = it == filterOrder
                    }
                cgOrder.addView(chip)
            }
        }
    }
}
package yu.desk.mococomic.presentation.component

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import yu.desk.mococomic.databinding.FragmentTabGenreBinding
import yu.desk.mococomic.utils.FilterGenre

class TabGenre : Fragment() {
	private lateinit var binding: FragmentTabGenreBinding
	var filterGenres: List<FilterGenre> = emptyList()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentTabGenreBinding.inflate(layoutInflater, container, false)
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
		Log.e("GENRE", "RESUME")
	}

	private fun updateFilter() {
		if (this::binding.isInitialized) {
			filterGenres.forEach {
				binding.cgGenre.check(it.id)
			}
		}
	}

	private fun initListener() {
		binding.apply {
			cgGenre.setOnCheckedStateChangeListener { group, checkedIds ->
				val checkedGenre = mutableListOf<FilterGenre>()
				checkedIds.forEach {
					FilterGenre.getById(it)?.let { genre ->
						checkedGenre.add(genre)
					}
				}
				filterGenres = checkedGenre
			}
		}
	}

	private fun initView() {
		binding.apply {
			FilterGenre.entries.forEach {
				val chip =
					Chip(requireContext(), null).apply {
						val drawable =
							ChipDrawable.createFromAttributes(
								requireContext(),
								null,
								0,
								com.google.android.material.R.style.Widget_Material3_Chip_Filter,
							)
						setChipDrawable(drawable)
						text = it.title
						id = it.id
					}
				cgGenre.addView(chip)
			}
		}
	}
}
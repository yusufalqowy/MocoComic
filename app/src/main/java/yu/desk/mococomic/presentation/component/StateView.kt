package yu.desk.mococomic.presentation.component

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.LayoutStateViewBinding
import yu.desk.mococomic.utils.setVisible

class StateView
	@JvmOverloads
	constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
		private val binding = LayoutStateViewBinding.inflate(LayoutInflater.from(context), this)
		private var state: State = State.CONTENT
		private var contentView: MutableList<View> = mutableListOf()
		private var onActionClickListener: () -> Unit = {}
		private var onBackClickListener: () -> Unit = {}
		val loadingView = binding.loadingView
		val alertView = binding.alertView

		init {
			val attributes = context.obtainStyledAttributes(attrs, R.styleable.StateView)
			try {
				state =
					attributes.getInt(R.styleable.StateView_state, State.CONTENT.ordinal).let {
						State.entries[it]
					}
				initView()
				this.layoutTransition =
					LayoutTransition().apply {
						enableTransitionType(LayoutTransition.CHANGING)
					}
			} finally {
				attributes.recycle()
			}
		}

		private fun initView() {
			binding.apply {
				alertView.addOnBackClickListener(onBackClickListener)
				alertView.addOnActionClickListener(onActionClickListener)
				when (state) {
					State.LOADING -> {
						binding.alertView.setVisible(false)
						binding.loadingView.setVisible(true)
						setVisibleContentView(false)
					}

					State.ERROR -> {
						binding.alertView.setVisible(true)
						binding.loadingView.setVisible(false)
						setVisibleContentView(false)
						setError()
					}

					State.EMPTY -> {
						binding.alertView.setVisible(true)
						binding.loadingView.setVisible(false)
						setVisibleContentView(false)
						setEmpty()
					}

					State.CONTENT -> {
						binding.alertView.setVisible(false)
						binding.loadingView.setVisible(false)
						setVisibleContentView(true)
					}

					State.ALERT -> {
						binding.alertView.setVisible(true)
						binding.loadingView.setVisible(false)
						setVisibleContentView(false)
						setAlert()
					}
				}
			}
			invalidate()
		}

		private fun setVisibleContentView(isVisible: Boolean) {
			contentView.forEach {
				it.setVisible(isVisible)
			}
		}

		override fun onViewAdded(child: View?) {
			super.onViewAdded(child)
			child?.let {
				if (it.id != R.id.loadingView && it.id != R.id.alertView) {
					contentView.add(it)
				}
			}
		}

		fun setError(description: String? = null, actionButtonText: String? = null) {
			binding.alertView.setAlertData(iconRes = R.drawable.ic_error, title = context.getString(R.string.text_something_went_wrong), description = description ?: context.getString(R.string.text_unexpected_error), actionButtonText = actionButtonText ?: context.getString(R.string.text_retry))
		}

		fun setEmpty(description: String? = null, actionButtonText: String? = null) {
			binding.alertView.setAlertData(iconRes = R.drawable.ic_empty, title = context.getString(R.string.text_it_empty), description = description ?: context.getString(R.string.text_there_nothing_here), actionButtonText = actionButtonText)
		}

		fun setAlert(
			@DrawableRes iconRes: Int = 0,
			title: String? = null,
			description: String? = null,
			actionButtonText: String? = null,
		) {
			binding.alertView.setAlertData(iconRes = iconRes, title = title, description = description, actionButtonText = actionButtonText)
		}

		fun setToolbar(title: String? = null, subtitle: String? = null) {
			binding.alertView.setAlertData(toolbarTitle = title, toolbarSubtitle = subtitle)
		}

		fun setIsLoading(isLoading: Boolean) {
			if (isLoading) {
				setState(State.LOADING)
			}
		}

		fun setIsEmpty(isEmpty: Boolean) {
			if (isEmpty) {
				setState(State.EMPTY)
			}
		}

		fun setState(state: State) {
			this.state = state
			initView()
		}

		fun getState() = state

		fun addOnActionClickListener(onActionClickListener: () -> Unit) {
			this.onActionClickListener = onActionClickListener
		}

		fun addOnBackClickListener(onBackClickListener: () -> Unit) {
			this.onBackClickListener = onBackClickListener
		}

		companion object {
			val LOADING = State.LOADING
			val ERROR = State.ERROR
			val ALERT = State.ALERT
			val EMPTY = State.EMPTY
			val CONTENT = State.CONTENT
		}

		enum class State {
			LOADING,
			ERROR,
			ALERT,
			EMPTY,
			CONTENT,
		}
	}
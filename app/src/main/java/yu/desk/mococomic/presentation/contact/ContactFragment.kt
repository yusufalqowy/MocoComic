package yu.desk.mococomic.presentation.contact

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import yu.desk.mococomic.databinding.FragmentContactBinding

class ContactFragment : Fragment() {
	lateinit var binding: FragmentContactBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		binding = FragmentContactBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initListener()
	}

	private fun initListener() {
		binding.apply {
			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}
			itemEmail.setOnClickListener {
				val recipient = "your_email@example.com"
				val subject = "Feedback from Moco Comic App"
				val message = "Dear Moco Comic Team,\n\nI have some feedback about your app...\n\n"
				val emailIntent =
					Intent(Intent.ACTION_VIEW, Uri.parse("mailto:")).apply {
						putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
						putExtra(Intent.EXTRA_SUBJECT, subject)
						putExtra(Intent.EXTRA_TEXT, message)
					}
				if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
					startActivity(Intent.createChooser(emailIntent, "Select Email App"))
				} else {
					Toast.makeText(requireContext(), "No email app found. Please download one!", Toast.LENGTH_SHORT).show()
					val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=mail&c=apps"))
					startActivity(playStoreIntent)
				}
			}

			itemDiscord.setOnClickListener {
				val discordInviteLink = "https://discord.gg/SERVER_KEY"
				val discordPackageName = "com.discord"

				val intent = requireActivity().packageManager.getLaunchIntentForPackage(discordPackageName)
				if (intent != null) {
					val discordIntent = Intent(Intent.ACTION_VIEW, Uri.parse(discordInviteLink))
					startActivity(discordIntent)
				} else {
					Toast.makeText(requireContext(), "Discord app is not installed", Toast.LENGTH_SHORT).show()
					val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$discordPackageName"))
					startActivity(playStoreIntent)
				}
			}
		}
	}
}
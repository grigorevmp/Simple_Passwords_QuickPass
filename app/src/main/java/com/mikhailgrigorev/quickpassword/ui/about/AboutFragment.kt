package com.mikhailgrigorev.simple_password.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhailgrigorev.simple_password.R
import com.mikhailgrigorev.simple_password.common.utils.*
import com.mikhailgrigorev.simple_password.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val view = binding.root
        initListeners()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListeners() {
        binding.ivTelegram.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(authorTelegramLink))
            startActivity(i)
        }

        binding.cvGitHub.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(authorGitHubLink))
            startActivity(i)
        }

        binding.ivSendMailToAuthor.setOnClickListener {
            sendEmail()
        }

        binding.tvTranslator.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(Dg_WPX_GitHubLink))
            startActivity(i)
        }

        binding.cvTranslators.setOnClickListener {
            binding.tvTranslator.visibility = View.VISIBLE
            binding.ivShowContributors.alpha = 0.5f
        }
    }

    private fun sendEmail() {
        val recipient = authorBaseMail
        val subject = "simple_password app: feedback"
        val message = "Hello, Mikhail \n"

        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        mIntent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            startActivity(Intent.createChooser(mIntent, getString(R.string.chooseEmail)))
        } catch (e: Exception) {
            e.message?.let { Utils.makeToast(requireContext(), it) }
        }
    }
}
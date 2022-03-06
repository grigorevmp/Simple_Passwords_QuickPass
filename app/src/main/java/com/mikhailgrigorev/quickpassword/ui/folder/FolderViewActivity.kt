package com.mikhailgrigorev.quickpassword.ui.folder

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikhailgrigorev.quickpassword.common.base.MyBaseActivity
import com.mikhailgrigorev.quickpassword.data.dbo.PasswordCard
import com.mikhailgrigorev.quickpassword.databinding.ActivityFolderViewBinding
import com.mikhailgrigorev.quickpassword.ui.main_activity.adapters.PasswordAdapter
import com.mikhailgrigorev.quickpassword.ui.password_card.view.PasswordViewActivity

class FolderViewActivity : MyBaseActivity() {
    private lateinit var binding: ActivityFolderViewBinding
    private lateinit var passwordCards: List<PasswordCard>
    private lateinit var viewModel: FolderViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
        initViewModel()
        setListeners()
        setObservers()

    }

    private fun initLayout() {
        binding.rvPasswordRecycler.setHasFixedSize(true)
        binding.rvPasswordRecycler.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        )
    }

    private fun setListeners() {
        binding.ivBackButton.setOnClickListener {
            finish()
        }
    }

    private fun setObservers() {
        val args: Bundle? = intent.extras
        val folderId = args?.get("folder_id").toString().toInt()
        val folderName = args?.get("folder_name").toString()
        binding.tvFolderName.text = folderName

        viewModel.getPasswordsFromFolder(folderId).observe(this) { passwords ->
            passwordCards = passwords
            binding.rvPasswordRecycler.adapter = PasswordAdapter(
                    passwords,
                    this,
                    clickListener = {
                        passClickListener(it)
                    },
                    longClickListener = { i: Int, view: View ->
                        passLongClickListener(
                                i,
                                view
                        )
                    }
            ) {
                tagSearchClicker(it)
            }
        }
    }

    private fun tagSearchClicker(string: String) {}


    private fun passLongClickListener(i: Int, view: View) {}

    private fun passClickListener(position: Int) {
        val intent = Intent(this, PasswordViewActivity::class.java)
        intent.putExtra("password_id", passwordCards[position]._id)
        startActivity(intent)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
                this,
                FolderViewModelFactory()
        )[FolderViewModel::class.java]
    }
}
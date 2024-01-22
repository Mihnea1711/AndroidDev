package com.example.trelloclone.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.adapters.BoardItemsAdapter
import com.example.trelloclone.databinding.ActivityMainBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null
    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    companion object {
        const val MY_PROFILE_REQUEST_CODE = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.TRELOL_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) { token ->
                updateFCMToken(token)
            }
        }

        FirestoreClass().loadUserData(this, true)

        binding?.drawerLayout?.findViewById<FloatingActionButton>(R.id.fab_create_board)?.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    private fun setupActionBar() {
        val mainToolbar: Toolbar? = binding?.drawerLayout?.findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(mainToolbar)
        mainToolbar?.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        mainToolbar?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        val drawer: DrawerLayout? = binding?.drawerLayout
        if(drawer?.isDrawerOpen(GravityCompat.START) == true) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer?.openDrawer(GravityCompat.START)
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean) {
        hideProgressDialog()
        val navUserImage: CircleImageView? = binding?.navView?.findViewById(R.id.iv_user_image)
        val navUserName: TextView? = binding?.navView?.findViewById(R.id.tv_username)

        mUserName = user.name

        if (navUserImage != null) {
            Glide
                .with(this@MainActivity)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(navUserImage)
        }

        if (navUserName != null) {
            navUserName.text = user.name
        }

        if (readBoardList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardList(this)
        }
    }

    fun populateBoardListInUI(boardList: ArrayList<Board>) {
        hideProgressDialog()
        val rvBoards: RecyclerView? = binding?.drawerLayout?.findViewById(R.id.rv_boards_list)
        val tvNoBoards: TextView? = binding?.drawerLayout?.findViewById(R.id.tv_no_boards_available)

        if (rvBoards != null && tvNoBoards != null) {
            rvBoards.layoutManager = LinearLayoutManager(this)

            if (boardList.size > 0) {

                rvBoards.visibility = View.VISIBLE
                tvNoBoards.visibility = View.GONE

                rvBoards.setHasFixedSize(true)

                val adapter = BoardItemsAdapter(this, boardList)
                rvBoards.adapter = adapter
                adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                    override fun onClick(position: Int, model: Board) {
                        val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                        intent.putExtra(Constants.DOCUMENT_ID, model.documentID)
                        startActivity(intent)
                    }
                })
            } else {
                rvBoards.visibility = View.GONE
                tvNoBoards.visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout? = binding?.drawerLayout
        if(drawer?.isDrawerOpen(GravityCompat.START) == true) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this@MainActivity, ProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this@MainActivity)
        } else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardList(this@MainActivity)
        }
        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}
package com.example.trelloclone.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trelloclone.activities.CardDetailsActivity
import com.example.trelloclone.activities.CreateBoardActivity
import com.example.trelloclone.activities.MainActivity
import com.example.trelloclone.activities.MembersActivity
import com.example.trelloclone.activities.ProfileActivity
import com.example.trelloclone.activities.SignInActivity
import com.example.trelloclone.activities.SignUpActivity
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore
            .collection(Constants.USERS_COLLECTION_NAME)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {e ->
                Log.e(activity.javaClass.simpleName, "Error writing doc: ${e.message}")
            }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        mFireStore
            .collection(Constants.USERS_COLLECTION_NAME)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {document ->
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null) {
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                        is ProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener {e ->
                when (activity) {
                    is SignInActivity  -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error writing doc: ${e.message}")
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore
            .collection(Constants.USERS_COLLECTION_NAME)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully.")
                Toast.makeText(activity, "Profile updated!", Toast.LENGTH_SHORT).show()
                when(activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is ProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { err ->
                when(activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is ProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board..", err)
                Toast.makeText(activity, "Error updating profile!", Toast.LENGTH_SHORT).show()
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if(currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun createBoard(activity: CreateBoardActivity, boardInfo: Board) {
        mFireStore
            .collection(Constants.BOARDS_COLLECTION_NAME)
            .document()
            .set(boardInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully")
                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener {e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error writing doc: ${e.message}")
                Toast.makeText(activity, "Board creation error", Toast.LENGTH_SHORT).show()

            }
    }

    fun getBoardList(activity: MainActivity) {
        mFireStore
            .collection(Constants.BOARDS_COLLECTION_NAME)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                activity.hideProgressDialog()
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<Board> = arrayListOf()
                for(item in document.documents) {
                    val board = item.toObject(Board::class.java)
                    if (board != null) {
                        board.documentID = item.id
                        boardList.add(board)
                    }
                }
                activity.populateBoardListInUI(boardList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board")
            }
    }

    fun getBoardDetailsByID(activity: TaskListActivity, boardDocumentID: String) {
        mFireStore
            .collection(Constants.BOARDS_COLLECTION_NAME)
            .document(boardDocumentID)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)
                if (board != null) {
                    board.documentID = document.id
                    activity.displayBoardDetails(board)
                }
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board")
            }
    }

    fun updateTaskList(activity: Activity, board: Board) {
        val taskListHash = HashMap<String, Any>()
        taskListHash[Constants.TASK_LIST] = board.taskList

        mFireStore
            .collection(Constants.BOARDS_COLLECTION_NAME)
            .document(board.documentID)
            .update(taskListHash)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Task list updated successfully")
                if (activity is TaskListActivity) {
                    activity.updateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.updateTaskListSuccess()
                }
            }
            .addOnFailureListener {
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Task list updated failure", it)
            }
    }

    fun getAssignedMemberListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore
            .collection(Constants.USERS_COLLECTION_NAME)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val userList: ArrayList<User> = arrayListOf()
                for (item in document.documents) {
                    val user = item.toObject(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }
                if (activity is MembersActivity) {
                    activity.setupMemberList(userList)
                } else if (activity is TaskListActivity) {
                    activity.getBoardMembersDetailList(userList)
                }
            }
            .addOnFailureListener {
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while loading members", it)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore
            .collection(Constants.USERS_COLLECTION_NAME)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    document.documents[0].toObject(User::class.java).let {
                        val user = it
                        if (user != null) {
                            activity.displayMemberDetails(user)
                        } else {
                            activity.hideProgressDialog()
                            activity.showErrorSnackBar("No such member found")
                        }
                    }
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting user details", it)
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHash = HashMap<String, Any>()
        assignedToHash[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore
            .collection(Constants.BOARDS_COLLECTION_NAME)
            .document(board.documentID)
            .update(assignedToHash)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while assigning member to board", it)
            }
    }
}
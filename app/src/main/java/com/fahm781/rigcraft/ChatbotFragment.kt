package com.fahm781.rigcraft

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fahm781.rigcraft.chatbotServices.BOT_MSG
import com.fahm781.rigcraft.chatbotServices.ChatbotRepository
import com.fahm781.rigcraft.chatbotServices.MY_MSG
import com.google.firebase.firestore.FirebaseFirestore
import com.fahm781.rigcraft.chatbotServices.Msg
import com.fahm781.rigcraft.chatbotServices.MsgAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import kotlin.random.Random


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatbotFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatbotFragment : Fragment() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var welcomeText: TextView
    private lateinit var msgList: ArrayList<Msg>
    private lateinit var msgAdapter: MsgAdapter
    private var chatbotRepository = ChatbotRepository()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=  inflater.inflate(R.layout.fragment_chatbot, container, false)

        // Initialise the button and set the click listener
        buttonSend = view.findViewById(R.id.buttonSend)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView)
        welcomeText = view.findViewById(R.id.welcomeText)
        msgList = ArrayList()

        //setup recycler view
        msgAdapter = MsgAdapter(msgList)
        chatRecyclerView.adapter = msgAdapter
        LinearLayoutManager(requireContext()).also { linearLayoutManager ->
            linearLayoutManager.stackFromEnd = true
            chatRecyclerView.layoutManager = linearLayoutManager
        }

        //send the message to the chatbot and add it to the recycler view
        buttonSend.setOnClickListener {
            val query = editTextMessage.text.toString().trim()
            if (query.isNotEmpty()) {
                addMessage(query, MY_MSG)
                editTextMessage.setText("")
                val prompt = "Answer queries only related to PC building and such. Otherwise, say 'I can only answer queries related to PC building'"

                welcomeText.visibility = View.GONE
                // Get the response from the chatbot and add it to the recycler view
                chatbotRepository.getResponse(query, prompt) { result ->
                    addMessage(result, BOT_MSG)
                }
                // Generate a random integer between 1 and 100
                val randomNumber = Random.nextInt(1, 101)

                // Show the disclaimer if the user is new or at random (20% chance)
                if (isFirstTimeUser() || randomNumber <= 20) {
                    showDisclaimer()
                    setNotFirstTimeUser()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a query", Toast.LENGTH_SHORT).show()
            }
        }
        //If there is previous chat history, load it and add it to the recycler view
        loadMessagesFromFirestore()
        return view
    }

    //add message to the recycler view/MsgAdapter
    private fun addMessage(message: String, sentBy: String) {
        activity?.runOnUiThread {
            saveMessageToFirestore(sentBy, message)
            msgList.add(Msg(sentBy, message))
            msgAdapter.notifyDataSetChanged()
            chatRecyclerView.smoothScrollToPosition(msgAdapter.itemCount - 1)
        }
    }
    //this method saves the messages to the firestore database
    private fun saveMessageToFirestore(sentBy: String, message: String) {

        val msg = hashMapOf(
            "sentBy" to sentBy,
            "message" to message,
            "User UID" to FirebaseAuth.getInstance().currentUser?.uid,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("ChatbotMessages")
            .add(msg)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    //this method will load the previous messages (if any) from the firestore database
    private fun loadMessagesFromFirestore() {
        //need to check first if there are any ChatbotMessages in the database
        db.collection("ChatbotMessages")
            .whereEqualTo("User UID", FirebaseAuth.getInstance().currentUser?.uid)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(150) //can change later to 90
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    return@addOnSuccessListener // if no documents are found return from the method
                }
                for (document in documents) {
                    val sentBy = document.getString("sentBy") ?: ""
                    val message = document.getString("message") ?: ""
                    msgList.add(Msg(sentBy, message))
                }
                // If the total number of messages exceeds 90, delete the oldest messages
                val excess = documents.size() - 90
                if (excess > 0) {
                    // If the total number of messages exceeds 90, delete the oldest messages
                    val messagesToDelete = documents.documents.take(excess)
                    for (message in messagesToDelete) {
                        db.collection("ChatbotMessages").document(message.id).delete()
                    }
                }
                welcomeText.visibility = View.GONE
                msgAdapter.notifyDataSetChanged()
                chatRecyclerView.smoothScrollToPosition(msgAdapter.itemCount - 1)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    // store a flag indicating whether the user is new or not
    private fun isFirstTimeUser(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("ChatbotPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("firstTimeUser", true)
    }

    // set the flag to indicate that the user is not new
    private fun setNotFirstTimeUser() {
        val sharedPreferences = requireActivity().getSharedPreferences("ChatbotPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("firstTimeUser", false).apply()
    }

    // show a disclaimer to the user stating that the chatbot responses may not be accurate at times
    private fun showDisclaimer() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Disclaimer")
        builder.setMessage("Please note that sometimes the Chatbot may give inaccurate answers as this feature is still a work in progress.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    }




package com.sumanth.tempmail

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.sumanth.tempmail.adapter.EmailrecyclerViewAdapter
import com.sumanth.tempmail.data.EmailDataModel
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest


class MainActivity : AppCompatActivity() {

    lateinit var mResquestQueue: RequestQueue
    lateinit var emailsAdapter : EmailrecyclerViewAdapter
    private var emailList = ArrayList<EmailDataModel>()
    //TODO("Go to the https://rapidapi.com/Privatix/api/temp-mail and get your secret API key in order to use the temp mail API ")
    val TEMP_MAIL_API_KEY : String = "Put your secret API key here"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGetDomains = findViewById<Button>(R.id.btnGetDomains)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnCreateMail = findViewById<Button>(R.id.btnCreateMail)
        val btnCopy = findViewById<Button>(R.id.btnCopy)
        val btnGetEmails = findViewById<Button>(R.id.btnGetEmails)
        val rvEmails = findViewById<RecyclerView>(R.id.rvEmails)
        val rgDomains = findViewById<RadioGroup>(R.id.rgDomains)
        val rbDomain1 = findViewById<RadioButton>(R.id.rbDomain1)
        val rbDomain2 = findViewById<RadioButton>(R.id.rbDomain2)
        val rbDomain3 = findViewById<RadioButton>(R.id.rbDomain3)
        val rbDomain4 = findViewById<RadioButton>(R.id.rbDomain4)

        var domain1 = ""
        var domain2= ""
        var domain3 = ""
        var domain4 = ""
        var hashEmail  = ""


        btnCreateMail.isEnabled = false
        btnGetEmails.isEnabled = false
        rgDomains.isVisible = false
        tvEmail.isVisible = false
        btnCopy.isVisible = false
        mResquestQueue = Volley.newRequestQueue(this)

        //rv

        emailsAdapter = EmailrecyclerViewAdapter(emailList)
        val layoutManager = LinearLayoutManager(applicationContext)
        rvEmails.layoutManager = layoutManager
        rvEmails.itemAnimator = DefaultItemAnimator()
        rvEmails.adapter = emailsAdapter


        btnGetDomains.setOnClickListener{

            Toast.makeText( this,"Getting domain names. Please wait....", Toast.LENGTH_LONG).show()

            val url = "https://privatix-temp-mail-v1.p.rapidapi.com/request/domains/"

            val queue = Volley.newRequestQueue(this)
            val postRequest: StringRequest = object : StringRequest(Method.GET, url,
                Response.Listener { response -> // response
                    Log.d("Response", response)
                    val jsonarry = JSONArray(response)
                    domain1 = jsonarry.get(0) as String
                    domain2 = jsonarry.get(1) as String
                    domain3 = jsonarry.get(2) as String
                    domain4 = jsonarry.get(3) as String

                    rbDomain1.text = domain1
                    rbDomain2.text = domain2
                    rbDomain3.text = domain3
                    rbDomain4.text = domain4

                    rgDomains.visibility   = View.VISIBLE
                    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                    //starting the animation
                    rgDomains.startAnimation(animation)
                    rbDomain1.isChecked = true
                    btnCreateMail.isEnabled = true

                },
                Response.ErrorListener { error -> // TODO Auto-generated method stub
                    Log.d("ERROR", "error => $error")
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["x-rapidapi-key"] = TEMP_MAIL_API_KEY
                    params["x-rapidapi-host"] = "privatix-temp-mail-v1.p.rapidapi.com"
                    return params
                }
            }
            queue.add(postRequest)

        }








        btnCreateMail.setOnClickListener {
            val email = getRandomString(10)
            val checkedButton =findViewById<RadioButton>(rgDomains.checkedRadioButtonId)
            val emailid = email + checkedButton.text
            hashEmail = getHash(emailid)
            Log.d(TAG, "onCreate: md5 hash: "+hashEmail)
            tvEmail.setText(emailid)
            btnCopy.isVisible = true
            tvEmail.isVisible = true
            btnGetEmails.isEnabled = true
        }


        btnGetEmails.setOnClickListener {


            emailList.clear()

            emailsAdapter.notifyDataSetChanged()

            val url = "https://privatix-temp-mail-v1.p.rapidapi.com/request/mail/id/"+hashEmail+"/"

            val queue = Volley.newRequestQueue(this)
            val postRequest: StringRequest = object : StringRequest(Method.GET, url,
                    Response.Listener { response -> // response
                        Log.d("Response", response)

                        try {
                        for (i : Int in response.indices) {

                                val jsonArray = JSONArray(response)
                                val jsonObject = jsonArray.getJSONObject(i)
                                val mail_from = jsonObject.getString("mail_from")
                                val mail_subject = jsonObject.getString("mail_subject")
                                val mail_text = jsonObject.getString("mail_text")
                                Log.d(TAG, "onCreate: JSONArray : " + jsonArray)

                                Log.d(TAG, "onCreate: Mail Text :" + mail_text)
                                addEmailData(mail_from, mail_subject, mail_text)
                                emailsAdapter.notifyDataSetChanged()
                            }
                            }catch (e : Exception){
                                try {
                                    val jsonObject = JSONObject(response)
                                    val msg = jsonObject.getString("error")
                                    addEmailData(
                                        msg,
                                        "try again after few seconds !",
                                        " "
                                    )
                                    emailsAdapter.notifyDataSetChanged()
                                }catch (e : java.lang.Exception){
                                    e.printStackTrace()
                                }

                            }

                    },
                    Response.ErrorListener { error -> // TODO Auto-generated method stub
                        Log.d("ERROR", "error => $error")
                    }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()

                    params["x-rapidapi-key"] = TEMP_MAIL_API_KEY
                    params["x-rapidapi-host"] = "privatix-temp-mail-v1.p.rapidapi.com"
                    return params
                }
            }
            queue.add(postRequest)





        }

        btnCopy.setOnClickListener{
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("emial",tvEmail.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Email succesfully copied to clipboard !", Toast.LENGTH_SHORT)
                .show()
        }






    }

    private fun addEmailData(from : String,subject: String,body : String) {
    var email = EmailDataModel(from,subject,body)
        emailList.add(email)

    }

    private fun getHash(emailid: String):String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(emailid.toByteArray())).toString(16).padStart(32, '0')
    }


    fun getRandomString(length: Int) : String {
        val allowedChars =('a'..'z')
        return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
    }

}
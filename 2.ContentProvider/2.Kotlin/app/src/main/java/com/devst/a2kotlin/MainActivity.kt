package com.devst.a2kotlin

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader

//don't forget that in higher versions-->first take the permissions
///thw app that needs acess sends request via content resolver
// the resolver triggers the target app the target app returns response via retrninh a cursor and
//the aop that needs the acess now can do it via cursor
//a.request     b.load  c.initiilase ui
//a,c-->main thread pr
//b-->can't run on main thread
//one way is to do it via thread or aync
// jus enclode that part into corouitnes context swiched s
//till now we have just studied crud-->only retrive operation
//focus ony on crud operations

//next we are going to focus on making our own app capable of being provider now not the resolver
//code of add deleye and be found on documentation very easily just know thw concepts now.

//only topc lft--> making my own app a provider-->no ckear code was present -->study later
//resolver me itne hi arts the

//provider concept of deisgnin[brief]:
/*
* usd when we want our app to be provider   db / file data/ netwero data share
* now contnet uri plays a importamt role in this
* like there can be many resquests made to our app for compet[whole table ] partial[a particuar col ] and select data[ek hi record * ]
* content uri hepls us to handle different types of thee rquests
* uri has 3 components:
* content:
* authorty:package name
* path:user defined strig costats
*
*
* baaki content provider in kotlinmlater study:-->
*  */

class MainActivity : AppCompatActivity() {
    //private int MY_PERMISSIONS_REQUEST_READ_CONTACTS=20;
    private var firstTimeLoaded = false
    private var textViewQueryResult: TextView? = null
    private var buttonLoadData: Button? = null
    private var buttonAddContact: Button? = null
    private var buttonRemoveContact: Button? = null
    private var buttonUpdateContact: Button? = null
    private var contentResolver: ContentResolver? = null
    private var editTextContactName: EditText? = null
    private val mContactsLoader: CursorLoader? = null
    private val mColumnProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.CONTACT_STATUS,
        ContactsContract.Contacts.HAS_PHONE_NUMBER
    )
    private val mSelectionCluse = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " = ?"
    private val mSelectionArguments = arrayOf("Ajay")
    private val mOrderBy = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewQueryResult = findViewById<View>(R.id.textViewQueryResulttd) as TextView
        editTextContactName = findViewById<View>(R.id.editTextContactNameed) as EditText
        buttonLoadData = findViewById<View>(R.id.buttonLoadd) as Button
        buttonAddContact = findViewById<View>(R.id.buttonAddd) as Button
        buttonRemoveContact = findViewById<View>(R.id.buttonRemoved) as Button
        buttonUpdateContact = findViewById<View>(R.id.buttonUpdated) as Button
        buttonLoadData!!.setOnClickListener{

        }
        buttonAddContact!!.setOnClickListener{
            addContact()
        }
        buttonRemoveContact!!.setOnClickListener{
            removeContacts()
        }
        buttonUpdateContact!!.setOnClickListener{
            updateContact()
        }
        contentResolver = getContentResolver()
    }

    fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor>? {
        return if (i == 1) {
            CursorLoader(
                this,
                ContactsContract.Contacts.CONTENT_URI,
                mColumnProjection,
                null,
                null,
                null
            )
        } else null
    }

    fun onLoadFinished(loader: Loader<Cursor?>?, cursor: Cursor?) {
        if (cursor != null && cursor.count > 0) {
            val stringBuilderQueryResult = StringBuilder("")
            while (cursor.moveToNext()) {
                stringBuilderQueryResult.append(
                    """${cursor.getString(0)} , ${cursor.getString(1)} , ${cursor.getString(2)} , ${
                        cursor.getString(
                            3
                        )
                    }
"""
                )
            }
            textViewQueryResult!!.text = stringBuilderQueryResult.toString()
        } else {
            textViewQueryResult!!.text = "No Contacts in device"
        }
    }
/*
    fun onLoaderReset(loader: Loader<Cursor?>?) {}
    fun onClick(view:View) {
        when (view.id) {
            R.id.buttonLoadd -> if (firstTimeLoaded == false) {
                loaderManager.initLoader(1, null, this)
                firstTimeLoaded = true
            } else {
                loaderManager.restartLoader(1, null, this)
            }
            R.id.buttonAddd -> addContact()
            R.id.buttonRemoved -> removeContacts()
            R.id.buttonUpdated -> updateContact()
            else -> {}
        }
    }
    */


    private fun addContact() {
        val cops = ArrayList<ContentProviderOperation>()

        //this libararry cp opertaions aadds operations on content provider methds
        cops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "accountname@gmail.com")
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "com.google")
                .build()
        )

        // here code of content provider operation -->added methids on cp operatons
        cops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    editTextContactName!!.text.toString()
                )
                .build()
        )
        try {

            //main code of the cr
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, cops)
        } catch (exception: Exception) {
            Log.i("xxx", exception.message!!)
        }
    }

    private fun updateContact() {
        val updateValue = editTextContactName!!.text.toString().split(" ".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val result: Array<ContentProviderResult>? = null
        var targetString: String? = null
        var newString: String? = null
        if (updateValue.size == 2) {
            targetString = updateValue[0]
            newString = updateValue[1]

            //ed text se name search
            val where = ContactsContract.RawContacts._ID + " = ? "
            val params = arrayOf(targetString)
            val contentResolver = getContentResolver()
            //content values-->for holding key value pairs that are to be added key->column
            val contentValues = ContentValues()
            contentValues.put(
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                newString
            ) //key value daaldi
            //now udate it .
            contentResolver.update(
                ContactsContract.RawContacts.CONTENT_URI,
                contentValues,
                where,
                params
            )
        }
    }

    private fun removeContacts() {
        //notethat earlier we used ContactsContract.Contact but here we used raw ?-->different content rvider used here why isit so?-->the delete update and add->alwys raw me hoga
        val whereClause = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + " = '" + editTextContactName!!.text.toString() + "'"
        getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, whereClause, null)
    }
}
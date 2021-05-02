package com.example.projectsingularity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import java.io.*
import java.time.Instant


@RequiresApi(Build.VERSION_CODES.O)
class FileIoFragment : Fragment() {
    private var stop = false
    private var outputStream: OutputStream? = null
    private var FILE_NAME = "HRT_DAT.txt"
    private var hrval: String? = "Copyright 2056 Faggot industries"
    private var running: Boolean = false

    companion object {
        private const val WRITE_DOCUMENT_REQUEST = 101
        private const val OPEN_DOCUMENT_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_file_io, container, false)
        val createfile = view.findViewById<Button>(R.id.butt_create_file)
        createfile.setOnClickListener { view1: View? ->
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TITLE, FILE_NAME)
            startActivityForResult(intent, WRITE_DOCUMENT_REQUEST)
        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WRITE_DOCUMENT_REQUEST) {
            if (resultCode != Activity.RESULT_OK) return
            val uri = data!!.data
            try {
                startrec(uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(FileNotFoundException::class, IOException::class)
    private fun startrec(uri: Uri?) {

        val startrec = requireView().findViewById<Button>(R.id.butt_start)
        outputStream = requireContext().contentResolver.openOutputStream(uri!!)
        val writer = BufferedWriter(OutputStreamWriter(outputStream))
        startrec.setOnClickListener { view: View? ->
            writer.write(hrval)
            writer.newLine()
            writer.flush()

            setFragmentResultListener("requestKey") { key, bundle ->
                val instant: Instant = Instant.now()
                val result = bundle.getString("key")
                hrval = result
                try {
                    writer.write(instant.toString() + ": " + hrval) //value
                    writer.newLine()
                    writer.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                stoprec(outputStream)
            }
        }
    }

    private fun stoprec(outputStream: OutputStream?) {
        val stoprec = requireView().findViewById<Button>(R.id.butt_stop)
        stoprec.setOnClickListener { view: View? ->
            try {
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            stop = true
        }
    }

}
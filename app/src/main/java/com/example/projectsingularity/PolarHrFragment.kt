package com.example.projectsingularity

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import polar.com.sdk.api.PolarBleApi
import polar.com.sdk.api.PolarBleApiCallback
import polar.com.sdk.api.PolarBleApiDefaultImpl
import polar.com.sdk.api.errors.PolarInvalidArgument
import polar.com.sdk.api.model.PolarDeviceInfo
import polar.com.sdk.api.model.PolarHrData
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.M)
class PolarHrFragment : Fragment() {
    private var conn = false
    private var scanDisposable: Disposable? = null
    private var polarHrAddress: String? = null
    var hrtval: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_polar_hr, container, false)
        val api = PolarBleApiDefaultImpl.defaultImplementation(requireContext(), PolarBleApi.ALL_FEATURES)
        api.setPolarFilter(false)
        val hrt = view.findViewById<TextView>(R.id.polar_hr_val)
        val connDiss = view.findViewById<Button>(R.id.butt_con_dis)
        val devid = view.findViewById<TextView>(R.id.device_id)
        api.setApiCallback(object : PolarBleApiCallback() {

            override fun hrNotificationReceived(identifier: String, data: PolarHrData) {
                Log.d(TAG, "HR value: " + data.hr + " rrsMs: " + data.rrsMs + " rr: " + data.rrs + " contact: " + data.contactStatus + "," + data.contactStatusSupported)
                hrt.text = String.format(Locale.ENGLISH, "%d", data.hr)
                hrtval = String.format(Locale.ENGLISH, "%d", data.hr)
                setFragmentResult("requestKey", bundleOf("key" to hrtval))
            }
        })

        val devicesAdapter =
            ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1)
        val dialog = Dialog(this.requireContext(), android.R.style.Theme_DeviceDefault_Dialog)
        dialog.setContentView(R.layout.ble_devices_popup)
        dialog.setCancelable(true)
        dialog.setTitle("Polar devices nearby")
        val listView = dialog.findViewById<ListView>(R.id.Discovered)
        listView.adapter = devicesAdapter
        connDiss.setOnClickListener {
            devicesAdapter.clear()
            if (scanDisposable == null) {
                dialog.show()
                scanDisposable = api.searchForDevice()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { polarDeviceInfo: PolarDeviceInfo ->
                        Log.d(TAG, "polar device found id: " + polarDeviceInfo.deviceId + " isConnectable: " + polarDeviceInfo.isConnectable)
                        if (polarDeviceInfo.deviceId.isNotEmpty()) {
                            devicesAdapter.add(polarDeviceInfo.name)
                            listView.onItemClickListener =
                                OnItemClickListener { parent: AdapterView<*>?, view1: View, position: Int, id: Long ->
                                    val info =
                                        (view1 as TextView).text.toString()
                                    polarHrAddress = info.substring(info.length - 8)
                                    devicesAdapter.clear()
                                    dialog.hide()
                                    connDiss.text = "Connecting"
                                    try {
                                        api.connectToDevice(polarHrAddress!!)
                                    } catch (polarInvalidArgument: PolarInvalidArgument) {
                                        polarInvalidArgument.printStackTrace()
                                    }
                                    conn = true
                                    connDiss.text = "Disconnect"
                                    //devid.setText(polarDeviceInfo.deviceId);
                                    devid.text = polarHrAddress
                                }
                        }
                    }
            } else if (conn) {
                scanDisposable!!.dispose()
                scanDisposable = null
                try {
                    api.disconnectFromDevice(polarHrAddress!!)
                } catch (polarInvalidArgument: PolarInvalidArgument) {
                    polarInvalidArgument.printStackTrace()
                }
                hrt.text = "No data"
                connDiss.text = "Connect"
                devid.text = "None"
            }
            dialog.setOnDismissListener {
                if (conn) {
                    scanDisposable!!.dispose()
                    devicesAdapter.clear()
                }
                devicesAdapter.clear()
                scanDisposable = null
                hrt.text = "No data"
                connDiss.text = "Connect"
                devid.text = "None"
            }
        }
        return view
    }

    companion object {
        private val TAG = PolarHrFragment::class.java.simpleName
    }
}
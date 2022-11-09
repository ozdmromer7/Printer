package com.eryaz.aceesswifidevices

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eryaz.aceesswifidevices.databinding.ActivityMainBinding
import java.net.InetAddress


class MainActivity : AppCompatActivity(){

    var gpsStatus: Boolean = false
    var network: Boolean = false
    var intent1: Intent? = null
    lateinit var wifiManager: WifiManager
    lateinit var results:ArrayList<ScanResult>
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WifiAdapter
    private lateinit var mServiceName:String
    private lateinit var nsdManager: NsdManager
    private lateinit var mService: NsdServiceInfo
    //private lateinit var nsdHelper: NsdHelper
    private val SERVICE_TYPE:String ="_services._dns-sd._udp"//this type shows that all devices in the network and "_ipp._tcp" this using this type we will reach only printer


    private val wifiScanReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    private val registrationListener = object : NsdManager.RegistrationListener{
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {


        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {

        }

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {

            if (serviceInfo != null) {
                mServiceName=serviceInfo.serviceName
            }
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {

        }
    }
    private val discoveryListener= object: NsdManager.DiscoveryListener{
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {

            Log.d(TAG,"Discovery Failed : Error code : $errorCode ")
            nsdManager.stopServiceDiscovery(this)

        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {

            Log.d(TAG,"Discovery Failed : Error code : $errorCode ")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onDiscoveryStarted(serviceType: String?) {

            Log.d(TAG,"Discovery Started:$serviceType ")
        }

        override fun onDiscoveryStopped(serviceType: String?) {

            Log.d(TAG,"Discovery Stopped:$serviceType ")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {

            Log.d(TAG,"Service Discovery success$serviceInfo")

            if (serviceInfo != null) {
                when{
                    serviceInfo.serviceType != SERVICE_TYPE ->
                        Log.d(TAG,"Unknown Service Type:${serviceInfo.serviceType}")

                    serviceInfo.serviceName == mServiceName ->
                        Log.d(TAG, "Same Machine: ${mServiceName}")
                    serviceInfo.serviceName.contains("NdsChat") -> nsdManager.resolveService(serviceInfo,resolveListener)
                }
                println(serviceInfo.serviceName)
            }

        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {

            Log.d(TAG,"Service Lost: $serviceInfo ")
        }
    }
    private val resolveListener= object :NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {

            Log.e(TAG,"Resolve Failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {

            Log.e(TAG,"Resolve Succeeded. $serviceInfo")

            if (serviceInfo != null) {
                if (serviceInfo.serviceName==mServiceName){

                    Log.e(TAG,"SAME IP.")
                    return
                }
                mService=serviceInfo
                val port : Int = serviceInfo.port
                val host: InetAddress = serviceInfo.host
            }

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        wifiManager =this.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        results= wifiManager.scanResults as ArrayList<ScanResult>

        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        adapter= WifiAdapter(results)
        binding.recyclerView.adapter=adapter

        //gpsStatus()
        locationPermission()
        locationEnabled()
//        nsdHelper = NsdHelper(this,this)
//        nsdHelper.isLogEnabled=true
//        nsdHelper.isAutoResolveEnabled=true
//        nsdHelper.setDiscoveryTimeout(30)

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
        val success = wifiManager.startScan()
        if (!success) {

             scanFailure()
        }
        registerService(3)

        nsdManager.discoverServices(SERVICE_TYPE,NsdManager.PROTOCOL_DNS_SD,discoveryListener)

    }

    override fun onPause() {
        tearDown()
        super.onPause()
    }

    override fun onResume() {

        super.onResume()
    }

    override fun onDestroy() {
        tearDown()
        super.onDestroy()
    }
    private fun locationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        network =locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (gpsStatus){
            Toast.makeText(this,"Location Enabled",Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this,"Location Disabled",Toast.LENGTH_SHORT).show()
        }
    }
    private fun scanSuccess() {

        Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
    }
    private fun scanFailure() {

        Toast.makeText(this,"Failure",Toast.LENGTH_SHORT).show()
    }

    private fun locationPermission(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION),100)
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION),101)
        }
    }
    fun gpsStatus() {
        intent1 = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent1)
        //for intent location service in phone
    }
    fun registerService(port:Int){
        val serviceInfo= NsdServiceInfo().apply {

            serviceName ="NsdChat"
            serviceType = SERVICE_TYPE
            setPort(port)
        }
        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {

            registerService(serviceInfo,NsdManager.PROTOCOL_DNS_SD,registrationListener)
        }
    }

    fun tearDown(){
        nsdManager.apply {
            unregisterService(registrationListener)
            stopServiceDiscovery(discoveryListener)
        }
    }

}

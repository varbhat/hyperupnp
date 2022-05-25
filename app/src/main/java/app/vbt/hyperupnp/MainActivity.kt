package app.vbt.hyperupnp

import android.content.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.vbt.hyperupnp.androidupnp.*
import app.vbt.hyperupnp.databinding.ActivityMainBinding
import app.vbt.hyperupnp.models.CustomListItem
import app.vbt.hyperupnp.models.DeviceModel
import app.vbt.hyperupnp.models.ItemModel
import app.vbt.hyperupnp.upnp.cling.model.meta.Service
import com.google.android.material.color.DynamicColors
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), Callbacks,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityMainBinding

    private var recyclerview: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var emptyIndicatorView: View? = null

    private var mService: AndroidUpnpService? = null

    private val mListener: BrowseRegistryListener = BrowseRegistryListener(this, mService, this)
    private val mFolders: Stack<ItemModel> = Stack<ItemModel>()

    private var mIsShowingDeviceList = true
        set(value) {
            field = value
            binding.toolbar.menu.findItem(R.id.action_go_up)?.isVisible = !value
            (emptyIndicatorView as TextView?)?.text =
                if (value) this.getText(R.string.device_empty) else this.getText(R.string.looks_empty)
        }
    private var mCurrentDevice: DeviceModel? = null

    private var mDeviceList: ArrayList<CustomListItem> = ArrayList()
    private var mItemList: ArrayList<CustomListItem> = ArrayList()
    private lateinit var mDeviceListAdapter: CustomListAdapter
    private lateinit var mItemListAdapter: CustomListAdapter


    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                val state = intent.getIntExtra(
                    WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN
                )
                when (state) {
                    WifiManager.WIFI_STATE_ENABLED -> {
                        refreshDevices()
                        refreshCurrent()
                    }
                    WifiManager.WIFI_STATE_DISABLED -> {
                        val mdlsize = mDeviceList.size
                        val milsize = mItemList.size
                        mDeviceList.clear()
                        mItemList.clear()
                        mDeviceListAdapter.notifyItemRangeRemoved(0, mdlsize)
                        mItemListAdapter.notifyItemRangeRemoved(0, milsize)
                    }
                    WifiManager.WIFI_STATE_UNKNOWN -> {
                        refreshDevices()
                        refreshCurrent()
                    }
                }
            }
        }
    }


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = service as AndroidUpnpService
            mService!!.registry.addListener(mListener)
            mService!!.registry.devices.forEach { device ->
                mListener.deviceAdded(device)
            }
            mService!!.controlPoint.search()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        DynamicColors.applyToActivitiesIfAvailable(application)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        recyclerview = findViewById<RecyclerViewPlus>(R.id.recyclerView)
        emptyIndicatorView = findViewById(R.id.looksempty)
        recyclerview?.layoutManager = GridLayoutManager(
            this,
            PreferenceManager.getDefaultSharedPreferences(this).getInt("settings_grid_count", 2)
        )
        mDeviceListAdapter = CustomListAdapter(this,mDeviceList,
            { c: CustomListItem -> navigateTo(c) }) { c: CustomListItem ->
            onLongClickCustomListItem(
                c
            )
        }
        mItemListAdapter = CustomListAdapter(this,mItemList,
            { c: CustomListItem -> navigateTo(c) }) { c: CustomListItem ->
            onLongClickCustomListItem(
                c
            )
        }
        mIsShowingDeviceList = true
        recyclerview?.adapter = mDeviceListAdapter
        refreshDevices()
        refreshCurrent()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
        val filter = IntentFilter()
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
        registerReceiver(receiver, filter)
        bindServiceConnection()

        swipeRefreshLayout = findViewById(R.id.swiperefresh)
        swipeRefreshLayout!!.setOnRefreshListener {
            if (mIsShowingDeviceList) refreshDevices() else refreshCurrent()
            swipeRefreshLayout!!.isRefreshing = false
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        binding.toolbar.menu.findItem(R.id.action_go_up)?.isVisible = false

        (menu.findItem(R.id.action_search).actionView as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (mIsShowingDeviceList) {
                    mDeviceListAdapter.filter.filter(query)
                } else {
                    mItemListAdapter.filter.filter(query)
                }
                return true
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                if (mIsShowingDeviceList) {
                    mDeviceListAdapter.filter.filter(newText)
                } else {
                    mItemListAdapter.filter.filter(newText)
                }
                return true
            }

        })


        // onEmpty and onNotEmpty is assigned here because
        // menu needs to be inflated before assigning
        // as we want to toggle visibility pick random option
        (recyclerview as RecyclerViewPlus?)?.onEmpty = {
            emptyIndicatorView?.visibility = View.VISIBLE
            binding.toolbar.menu.findItem(R.id.action_pick_random)?.isVisible = false
            binding.toolbar.menu.findItem(R.id.action_count_items)?.isVisible = false
            binding.toolbar.menu.findItem(R.id.action_shuffle)?.isVisible = false
        }
        (recyclerview as RecyclerViewPlus?)?.onNotEmpty = {
            emptyIndicatorView?.visibility = View.GONE
            binding.toolbar.menu.findItem(R.id.action_pick_random)?.isVisible = true
            binding.toolbar.menu.findItem(R.id.action_count_items)?.isVisible = true
            binding.toolbar.menu.findItem(R.id.action_shuffle)?.isVisible = true
        }
        return true
    }

    private fun onLongClickCustomListItem(item: CustomListItem): Boolean {
        if (item is DeviceModel) {
            AlertDialog.Builder(this)
                .setTitle("Choose an action:\n${item.title}")
                .setItems(
                    arrayOf(
                        "Open",
                        "Copy Device Title",
                        "Copy Base URL",
                        "Open Thumbnail URL",
                        "Copy Thumbnail URL"
                    )
                ) { _, which ->
                    when (which) {
                        0 -> navigateTo(item)
                        1 -> {
                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                                ClipData.newPlainText("Copied Device Title", item.title)
                            )
                        }
                        2 -> {
                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                                ClipData.newPlainText(
                                    "Copied Base URL",
                                    Uri.parse(item.iconUrl).host
                                )
                            )
                        }
                        3 -> {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data = Uri.parse(item.iconUrl)
                            startActivity(intent)
                        }
                        4 -> {
                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                                ClipData.newPlainText("Copied Thumbnail URL", item.iconUrl)
                            )
                        }
                    }
                }
                .create()
                .show()

        }
        if (item is ItemModel) {
            AlertDialog.Builder(this)
                .setTitle("Choose an action:\n${item.title}")
                .setItems(
                    arrayOf(
                        "Open/Stream",
                        "Copy Title",
                        "Copy Stream URL",
                        "Open Thumbnail URL",
                        "Copy Thumbnail URL"
                    )
                ) { _, which ->
                    when (which) {
                        0 -> if (item.isContainer) navigateTo(item) else item.Play()
                        1 -> {
                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                                ClipData.newPlainText("Copied Title", item.title)
                            )
                        }
                        2 -> {
                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                                ClipData.newPlainText("Copied Stream URL", item.url)
                            )
                        }
                        3 -> {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data = Uri.parse(item.iconUrl)
                            startActivity(intent)
                        }
                        4 -> {
                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                                ClipData.newPlainText("Copied Thumbnail URL", item.iconUrl)
                            )
                        }
                    }
                }
                .create()
                .show()
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_quit -> {
                finishAffinity()
                exitProcess(0)
            }
            R.id.action_refresh -> {
                if (mIsShowingDeviceList) refreshDevices() else refreshCurrent()
                return true
            }
            R.id.action_pick_random -> {
                return if (mIsShowingDeviceList) {
                    if (mDeviceListAdapter.customListFilterList.isNotEmpty()) onLongClickCustomListItem(
                        mDeviceListAdapter.customListFilterList.random()
                    ) else true
                } else {
                    if (mItemListAdapter.customListFilterList.isNotEmpty()) onLongClickCustomListItem(
                        mItemListAdapter.customListFilterList.random()
                    ) else true
                }
            }
            R.id.action_shuffle -> {
                if (mIsShowingDeviceList) {
                    mDeviceListAdapter.customListFilterList.shuffle()
                    mDeviceListAdapter.notifyItemRangeRemoved(0, mDeviceList.size)
                } else {
                    mItemListAdapter.customListFilterList.shuffle()
                    mItemListAdapter.notifyItemRangeRemoved(0, mItemList.size)
                }
                return true
            }
            R.id.action_count_items -> {
                AlertDialog.Builder(this)
                    .setTitle("Item Count")
                    .setMessage("There are total ${if (mIsShowingDeviceList) mDeviceListAdapter.itemCount else mItemListAdapter.itemCount} items")
                    .setPositiveButton("OK") { _, _ -> }
                    .create()
                    .show()
                return true
            }
            R.id.action_go_up -> {
                if (!(binding.toolbar.menu.findItem(R.id.action_search).actionView as SearchView).isIconified) binding.toolbar.collapseActionView()
                if (!mIsShowingDeviceList) onBackPressed()
                if (mIsShowingDeviceList) refreshDevices()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateTo(model: Any?) {
        if (model is DeviceModel) {
            if (!(binding.toolbar.menu.findItem(R.id.action_search).actionView as SearchView).isIconified) binding.toolbar.collapseActionView()
            val device = model.device
            if (device.isFullyHydrated) {
                val conDir = model.contentDirectory
                if (conDir != null) mService!!.controlPoint.execute(
                    CustomContentBrowseActionCallback(this, conDir, "0", this)
                )
                onDisplayDirectories()
                mIsShowingDeviceList = false
                mCurrentDevice = model
            } else {
                Toast.makeText(this, R.string.info_still_loading, Toast.LENGTH_SHORT).show()
            }
        }
        if (model is ItemModel) {
            if (model.isContainer) {
                if (!(binding.toolbar.menu.findItem(R.id.action_search).actionView as SearchView).isIconified) binding.toolbar.collapseActionView()
                if (mFolders.isEmpty()) mFolders.push(model) else if (mFolders.peek().id !== model.id) mFolders.push(
                    model
                )
                (model.service as Service<*, *>?)?.let {
                    mService!!.controlPoint.execute(
                        CustomContentBrowseActionCallback(
                            this,
                            it,
                            model.id,
                            this
                        )
                    )
                }
            } else {
                model.Play()
            }
        }
    }

    override fun onBackPressed() {
        if (goBack()) super.onBackPressed()
    }

    private fun goBack(): Boolean {
        if (mFolders.empty()) {
            if (!mIsShowingDeviceList) {
                mIsShowingDeviceList = true
                onDisplayDevices()
            } else {
                return true
            }
        } else {
            val item = mFolders.pop()
            mService!!.controlPoint.execute(
                CustomContentBrowseActionCallback(
                    this,
                    item.service as Service<*, *>,
                    item.container!!.parentID,
                    this
                )
            )
        }
        return false
    }

    fun refreshDevices() {
        if (mService == null) return
        if (!((binding.toolbar.menu.findItem(R.id.action_search).actionView as SearchView).isIconified)) return
        mService!!.registry.removeAllRemoteDevices()
        for (device in mService!!.registry.devices) mListener.deviceAdded(device)
        mService!!.controlPoint.search()
    }

    fun refreshCurrent() {
        if (mService == null) return
        if (!((binding.toolbar.menu.findItem(R.id.action_search).actionView as SearchView).isIconified)) return
        if (mIsShowingDeviceList) {
            onDisplayDevices()
            mService!!.registry.removeAllRemoteDevices()
            for (device in mService!!.registry.devices) mListener.deviceAdded(device)
            mService!!.controlPoint.search()
        } else {
            if (!mFolders.empty()) {
                val item = mFolders.peek() ?: return
                mService!!.controlPoint.execute(
                    CustomContentBrowseActionCallback(
                        this,
                        item.service as Service<*, *>,
                        item.id,
                        this
                    )
                )
            } else {
                if (mCurrentDevice != null) {
                    val service = mCurrentDevice!!.contentDirectory
                    if (service != null) mService!!.controlPoint.execute(
                        CustomContentBrowseActionCallback(this, service, "0", this)
                    )
                }
            }
        }
    }

    private fun bindServiceConnection(): Boolean {
        bindService(
            Intent(this, AndroidUpnpServiceImpl::class.java),
            serviceConnection, Context.BIND_AUTO_CREATE
        )
        return true
    }

    private fun unbindServiceConnection(): Boolean {
        if (mService != null) mService!!.registry.removeListener(mListener)
        unbindService(serviceConnection)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unbindServiceConnection()
    }

    override fun onDisplayDevices() {
        runOnUiThread {
            mIsShowingDeviceList = true
            recyclerview?.adapter = mDeviceListAdapter
        }
    }

    override fun onDisplayDirectories() {
        runOnUiThread {
            val milsize = mItemList.size
            mItemList.clear()
            mItemListAdapter.notifyItemRangeRemoved(0, milsize)
            mIsShowingDeviceList = false
            recyclerview?.adapter = mItemListAdapter
        }
    }

    override fun addItem(Item: ItemModel) {
        runOnUiThread {
            mItemList.add(Item)
            mItemListAdapter.notifyItemInserted(mItemList.size - 1)
        }
    }

    override fun clearItems() {
        runOnUiThread {
            val milsize = mItemList.size
            mItemList.clear()
            mItemListAdapter.notifyItemRangeRemoved(0, milsize)
        }
    }

    override fun itemError(error: String?) {
        runOnUiThread {
            val milsize = mItemList.size
            mItemList.clear()
            mItemListAdapter.notifyItemRangeRemoved(0, milsize)
            mItemList.add(
                CustomListItem(
                    R.drawable.ic_warning,
                    resources.getString(R.string.info_error_list_folders),
                    error
                )
            )
            mItemListAdapter.notifyItemInserted(mItemList.size - 1)
        }
    }

    override fun addDevice(device: DeviceModel) {
        runOnUiThread {
            val position: Int = mDeviceList.indexOf(device as CustomListItem)
            if (position >= 0) {
                mDeviceList.remove(device)
                mDeviceListAdapter.notifyItemRemoved(position)
                mDeviceListAdapter.notifyItemRangeChanged(position, mDeviceList.size - position)
                mDeviceList.add(position, device)
                mDeviceListAdapter.notifyItemInserted(position)
            } else {
                mDeviceList.add(device)
                mDeviceListAdapter.notifyItemInserted(mDeviceList.size - 1)
            }
        }
    }

    override fun rmDevice(device: DeviceModel) {
        runOnUiThread {
            val position: Int = mDeviceList.indexOf(device as CustomListItem)
            mDeviceList.remove(device)
            mDeviceListAdapter.notifyItemRemoved(position)
            mDeviceListAdapter.notifyItemRangeChanged(position, mDeviceList.size - position)
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1 != null && p1 == "settings_grid_count")
            recreate()
        if (p1 != null && p1 == "video_preview")
            recreate()
        if (p1 != null && p1 == "settings_validate_devices") {
            refreshDevices()
            refreshCurrent()
        }
    }
}
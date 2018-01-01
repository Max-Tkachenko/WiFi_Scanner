package com.example.max.wifi_scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private Element[] nets;
    private WifiManager wifi;
    private List<ScanResult> wifiList;
    private Switch aSwitch;
    private ListView listView;
    private Timer autoUpdate;
    private RelativeLayout scanLayout;
    private RelativeLayout connectingLayout;
    private boolean for_dialog = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main_layout);

        wifi=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        aSwitch = (Switch) findViewById(R.id.switch_button);
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifi.isWifiEnabled()) {
                    wifi.setWifiEnabled(false);
                }
                else {
                    wifi.setWifiEnabled(true);
                }
            }
        });
        listView = (ListView) findViewById(R.id.list);
        scanLayout = (RelativeLayout) findViewById(R.id.scanning_layout);
        scanLayout.setVisibility(View.GONE);
        connectingLayout = (RelativeLayout) findViewById(R.id.connecting_layout);
        connectingLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        refreshSwitch(aSwitch, wifi);
                        detectWifi();
                    }
                });
            }
        }, 0, 8500); // updates each 8.5 secs
    }

    public void closeApplication(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Are you sure to close application?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                close();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for_dialog = true;
                dialog.cancel();
            }
        });
        for_dialog = false;
        alert.show();
    }

    public void close() {
        this.finish();
    }

    private void detectWifi() {
        showProgress(wifi.isWifiEnabled());

        if(for_dialog) {
            this.wifi.startScan();
            this.wifiList = this.wifi.getScanResults();

            WifiInfo wifiInfo = wifi.getConnectionInfo();
            String name = wifiInfo.getSSID();

            this.nets = setElements(wifiList, name);

            AdapterElements adapterElements = new AdapterElements(this);
            if (wifi.isWifiEnabled() != false) {
                listView.setAdapter(adapterElements);
            } else {
                listView.setAdapter(null);
            }
        }
    }

    private void refreshSwitch(Switch aSwitch, WifiManager wifi) {
        if(wifi.isWifiEnabled() && !aSwitch.isChecked()) {
            aSwitch.setChecked(true);
        }
        else if(!wifi.isWifiEnabled() && aSwitch.isChecked()){
            aSwitch.setChecked(false);
        }
    }

    private void showProgress(boolean network) {
        if(network && for_dialog) {
            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        runOnUiThread(visibleLayout);
                        sleep(3500);
                        runOnUiThread(unVisibleLayout);
                    } catch (InterruptedException ex) { }
                }
            };
            thread.start();
        }
    }

    private void showConnecting() {
        for_dialog = false;
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    runOnUiThread(visibleConnecting);
                    sleep(5500);
                    runOnUiThread(unVisibleConnecting);
                    for_dialog = true;
                } catch (InterruptedException ex) { }

            }
        };
        thread.start();
    }

    public void openInfoDialog(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialog);
        alert.setTitle("Info");
        alert.setMessage("You can see list of all Wi-Fi networks around you. Tap on item that you need and click \"Connect\".");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for_dialog = true;
            }
        });
        for_dialog = false;
        alert.show();
    }

    private Element[] setElements(List<ScanResult> wifiList, String name) {
        Collections.sort(wifiList, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult sc1, ScanResult sc2) {
                return sc1.level > sc2.level ? -1 : (sc1.level < sc2.level) ? 1 : 0;
            }
        });
        Element[] networks = new Element[wifiList.size()];
        int indexWithNetwork = 0;
        for (int i = 0; i < wifiList.size(); i++) {
            String ssid;
            String item = wifiList.get(i).toString();
            try {
                String[] vector_item = item.split(",");
                String item_essid = vector_item[0];
                ssid = item_essid.split(": ")[1];
            }
            catch (Exception ex) {
                ssid = "Unknown network";
            }
            String security;

            int level = wifiList.get(i).level;

            if (item.contains("WPA2")) {
                security = "WPA2";
            }
            else if (item.contains("WPA")) {
                security = "WPA";
            }
            else if (item.contains("WEP")) {
                security = "WEP";
            }
            else {
                security = "No lock";
            }

            if(name.contains(ssid)) {
                networks[i] = new Element(ssid, security, level, true);
                indexWithNetwork = i;
            }
            else {
                networks[i] = new Element(ssid, security, level, false);
            }
        }

        if(indexWithNetwork != 0) {
            for (int j = 0; j < networks.length; j++) {
                if (networks[j].getNetwork() == true) {
                    indexWithNetwork = j;
                }
            }
            ArrayList<Element> arrayList = new ArrayList<>();
            arrayList.add(0, networks[indexWithNetwork]);

            for (int k = 0; k < networks.length - 1; k++) {
                if (networks[k].getNetwork() != true) {
                    arrayList.add(networks[k]);
                }
            }
            for (int l = 0; l < arrayList.size(); l++) {
                networks[l] = arrayList.get(l);
            }
        }

        return networks;
    }

    private int setImage(boolean network, boolean security, int i) {
        int resource = 0;
        if(network == true) {
            if (i > -50) {
                resource = R.drawable.wifi_icon;
            } else if (i <= -50 && i > -65) {
                resource = R.drawable.wifi_icon_middle_1;
            } else if (i <= -65 && i > -80) {
                resource = R.drawable.wifi_icon_low_1;
            } else if (i <= -80) {
                resource = R.drawable.wifi_icon_under_low_1;
            }
        }
        else {
            if (i > -50 && security == true) {
                resource = R.drawable.wifi_close_high;
            } else if (i <= -50 && i > -65 && security == true) {
                resource = R.drawable.wifi_icon_close_middle;
            } else if (i <= -65 && i > -80 && security == true) {
                resource = R.drawable.wifi_icon_close_low;
            } else if (i <= -80 && security == true) {
                resource = R.drawable.wifi_icon_close_under_low;
            }
            else if (i > -50) {
                resource = R.drawable.wifi_icon_high;
            } else if (i <= -50 && i > -65) {
                resource = R.drawable.wifi_icon_middle;
            } else if (i <= -65 && i > -80) {
                resource = R.drawable.wifi_icon_low;
            } else if (i <= -80) {
                resource = R.drawable.wifi_icon_under_low;
            }
        }
        return resource;
    }

    private boolean isClose(String sec) {
        if(sec.contains("WPA2")||sec.contains("WPA")||sec.contains("WEP")) {
            return true;
        }
        else {
            return false;
        }
    }

    private class AdapterElements extends ArrayAdapter<Object> {

        private Activity context;
        private TextView messageTv;
        private TextView passMessage;
        private EditText password;
        private CheckBox checkBox;

        public AdapterElements(Activity context) {
            super(context, R.layout.list_item, nets);
            this.context = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final LayoutInflater inflater = context.getLayoutInflater();
            final View item = inflater.inflate(R.layout.list_item, null);

            TextView tvName = (TextView) item.findViewById(R.id.name_tv);
            tvName.setText(nets[position].getTitle());

            TextView tvSecurity = (TextView) item.findViewById(R.id.security_text);
            final String security = nets[position].getSecurity();
            tvSecurity.setText(security);

            ImageView image = (ImageView) item.findViewById(R.id.signal_image);
            image.setImageResource(setImage(nets[position].getNetwork(), isClose(nets[position].getSecurity()), nets[position].getLevel()));

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean showConnectButton = true;
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialog);
                    dialog.setTitle(nets[position].getTitle());
                    final View forDialog = inflater.inflate(R.layout.item_dialog, null);
                    messageTv = (TextView) forDialog.findViewById(R.id.message);
                    passMessage = (TextView) forDialog.findViewById(R.id.pass_message);
                    password = (EditText)  forDialog.findViewById(R.id.password_et);
                    checkBox = (CheckBox) forDialog.findViewById(R.id.passCheck);
                    dialog.setView(forDialog);
                    if(nets[position].getNetwork()) {
                        messageTv.setText("You are connected to this network.");
                        passMessage.setVisibility(View.GONE);
                        password.setVisibility(View.GONE);
                        checkBox.setVisibility(View.GONE);
                        showConnectButton = false;
                    }
                    else if(isClose(nets[position].getSecurity())) {
                        messageTv.setVisibility(View.GONE);
                        passMessage.setText("Password:");
                    }
                    else {
                        messageTv.setText("Open Wi-Fi network.");
                        passMessage.setVisibility(View.GONE);
                        password.setVisibility(View.GONE);
                        checkBox.setVisibility(View.GONE);
                    }

                    if(showConnectButton) {
                        dialog.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                showConnecting();

                                WifiConfiguration wifiConfig = new WifiConfiguration();
                                wifiConfig.SSID = String.format("\"%s\"", nets[position].getTitle());
                                wifiConfig.preSharedKey = String.format("\"%s\"", password.getText().toString());
                                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

                                int netId = wifiManager.addNetwork(wifiConfig);
                                wifiManager.disconnect();
                                wifiManager.enableNetwork(netId, true);
                                wifiManager.reconnect();

                                for_dialog = true;
                            }
                        });
                    }

                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (!isChecked)
                                password.setTransformationMethod(new PasswordTransformationMethod());
                            else password.setTransformationMethod(null);
                            password.setSelection(password.length());
                        }
                    });

                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for_dialog = true;
                        }
                    });

                    for_dialog = false;
                    dialog.show();
                }
            });

            return item;
        }
    }

    private Runnable visibleLayout = new Runnable() {
        public void run() {
            scanLayout.setVisibility(View.VISIBLE);
        }
    };

    private Runnable unVisibleLayout = new Runnable() {
        public void run() {
            scanLayout.setVisibility(View.GONE);
        }
    };

    private Runnable visibleConnecting = new Runnable() {
        @Override
        public void run() { connectingLayout.setVisibility(View.VISIBLE);
        }
    };

    private Runnable unVisibleConnecting = new Runnable() {
        @Override
        public void run() { connectingLayout.setVisibility(View.GONE);
        }
    };
}

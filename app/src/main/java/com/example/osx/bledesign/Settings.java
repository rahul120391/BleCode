package com.example.osx.bledesign;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cocosw.bottomsheet.BottomSheet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by osx on 24/12/15.
 */
public class Settings extends Fragment {
    View v = null;

    @Bind(R.id.layout_settings_clicked)
    RelativeLayout layout_settings_clicked;

    @Bind(R.id.layout_settings)
    LinearLayout layout_settings;

    @Bind(R.id.btn_submit)
    Button btn_submit;
    Dialog dialog;

    @Bind(R.id.iv_pic)
    ImageView iv_pic;

    @Bind(R.id.tv_alerttonetype)
    TextView tv_alerttonetype;

    @Bind(R.id.tv_select_alert)
    TextView tv_select_alert;

    @Bind(R.id.tv_select_alert_type)
    TextView tv_select_alert_type;

    @Bind(R.id.tv_select_tone)
    TextView tv_select_tone;

    @Bind(R.id.tv_select_buzzer)
    TextView tv_select_buzzer;

    @Bind(R.id.et_name)
    EditText et_name;

    @Bind(R.id.tg_on_off)
    ToggleButton tg_on_off;
    private static final int GALLERY_CODE = 100;
    private static final int CAMERA_CODE = 200;
    Bitmap bitmap;
    String name, address, distance, location, uri,id;
    Bundle bundle;
    int alertvolume, buzzervolume;
    ArrayList<DeviceData> data;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, null);
        getActivity().setTitle(getString(R.string.tracker_detail));
        ButterKnife.bind(this, v);
        bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
            address = bundle.getString("address");
            if (bundle.getByteArray("image") != null) {
                bitmap = Utils.getbitmap(bundle.getByteArray("image"));
                iv_pic.setImageBitmap(bitmap);
            }
            et_name.setText(name);
            Mydatabase database = new Mydatabase(getActivity());
            data = database.getrecord(address);
            database.close();
            if (data.size() > 0) {
                distance = data.get(0).getDistance();
                if (distance!=null && !distance.equalsIgnoreCase("0")) {
                    tv_select_alert.setText(distance);
                }
                alertvolume = data.get(0).getVolume();
                buzzervolume = data.get(0).getBuzzer_volume();
                if (alertvolume == 1) {
                    tv_select_alert_type.setText("Low");
                } else if (alertvolume == 2) {
                    tv_select_alert_type.setText("High");
                }

                if (buzzervolume == 1) {
                    tv_select_alert_type.setText("Low");
                } else if (buzzervolume == 2) {
                    tv_select_buzzer.setText("High");
                }
                location = data.get(0).getTone_location();
                System.out.println("location"+location);
                ArrayList<HashMap<String, String>> list = Utils.listRingtones(getActivity());
                for(HashMap<String,String> map:list){
                    String uri=map.get("uri").toString();
                    String id=map.get("id").toString();
                    String loc=uri+"/"+id;
                    if(loc.equalsIgnoreCase(location)){
                        tv_select_tone.setText(map.get("title").toString());
                    }
                }
            }
        }

        tg_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialogg.getDialog(getActivity());
                if(MainActivity.mBearService!=null){
                    MainActivity.mBearService.BeepBuzzer(0);
                    BluetoothDevice device = Utils.getbluetoothdevice(address);
                    if (Utils.CheckConnectionState(getActivity(), address) == 0) {
                        System.out.println("connecting");
                        MainActivity.mBearService.connect(device, true);
                    } else if (Utils.CheckConnectionState(getActivity(), address) == 2) {
                        System.out.println("disconnecting");
                        MainActivity.mBearService.disconnect(device);
                    }
                }

            }
        });
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (Utils.CheckConnectionState(getActivity(), address) == 2) {
            tg_on_off.setChecked(true);
        } else if (Utils.CheckConnectionState(getActivity(), address) == 0) {
            tg_on_off.setChecked(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        System.out.println("inside on prepare optionsmenu addtracker");
        menu.clear();
    }

    @OnClick(R.id.btn_submit)
    public void Submit() {
        if (data.size() > 0) {
            ContentValues values = new ContentValues();
            values.put(Mydatabase.MAC_ADDRESS, address);
            values.put(Mydatabase.TRACKER_NAME, et_name.getText().toString().trim());
            values.put(Mydatabase.DISTANCE, tv_select_alert.getText().toString());
            if (tv_select_alert_type.getText().toString().equalsIgnoreCase("Low")) {
                values.put(Mydatabase.VOLUME_TYPE, 1);
            } else if (tv_select_alert_type.getText().toString().equalsIgnoreCase("High")) {
                values.put(Mydatabase.VOLUME_TYPE, 2);
            }
            values.put(Mydatabase.TONE_LOCATION, tv_select_tone.getText().toString());
            if (tv_select_buzzer.getText().toString().equalsIgnoreCase("Low")) {
                values.put(Mydatabase.BUZZER_TRACKER_VOLUME, 1);
            } else if (tv_select_buzzer.getText().toString().equalsIgnoreCase("High")) {
                values.put(Mydatabase.BUZZER_TRACKER_VOLUME, 2);
            }

            Bitmap resizebitmap=getResizedBitmap(bitmap, 300);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizebitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] image = stream.toByteArray();
            int sizeis=image.length;
            System.out.println("sizeis"+sizeis);
            values.put(Mydatabase.IMAGE_NAME, image);
            if(tv_select_tone.getText().toString().equalsIgnoreCase("default")){
                values.put(Mydatabase.TONE_LOCATION, tv_select_tone.getText().toString());
            }
            else{
                values.put(Mydatabase.TONE_LOCATION, uri+"/"+id);
            }

            Mydatabase database = new Mydatabase(getActivity());
            long result = database.UpdateTableData(values);
            if (result == -1) {
                Utils.ShowSnackBar(getActivity(), "Unable to update");
            } else {
                for(int i=0;i<MainActivity.devices.size();i++){
                    if(MainActivity.devices.get(i).getAddress().equalsIgnoreCase(address)){
                        if (tv_select_alert_type.getText().toString().equalsIgnoreCase("Low")) {
                            MainActivity.devices.get(i).setVolume(1);
                        } else if (tv_select_alert_type.getText().toString().equalsIgnoreCase("High")) {
                            MainActivity.devices.get(i).setVolume(2);
                        }
                        if (tv_select_buzzer.getText().toString().equalsIgnoreCase("Low")) {
                            MainActivity.devices.get(i).setBuzzer_volume(1);
                        } else if (tv_select_buzzer.getText().toString().equalsIgnoreCase("High")) {
                            MainActivity.devices.get(i).setBuzzer_volume(2);
                        }
                        if(tv_select_tone.getText().toString().equalsIgnoreCase("default")){
                            MainActivity.devices.get(i).setTone_location("default");
                        }
                        else{
                            MainActivity.devices.get(i).setTone_location(uri + "/" + id);
                        }
                        MainActivity.devices.get(i).setDistance(tv_select_alert.getText().toString());
                        MainActivity.devices.get(i).setImage(image);
                        MainActivity.devices.get(i).setName(et_name.getText().toString().trim());
                    }
                }
                Utils.ShowSnackBar(getActivity(), "Data updated successfully");
            }
            database.close();
        } else {
            Bitmap bitmap = ((BitmapDrawable) iv_pic.getDrawable()).getBitmap();
            Bitmap resizebitmap=getResizedBitmap(bitmap,300);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizebitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] image = stream.toByteArray();
            int sizeis=image.length;
            System.out.println("sizeis"+sizeis);
            ContentValues values = new ContentValues();
            System.out.println("address is" + address);
            values.put(Mydatabase.MAC_ADDRESS, address);
            values.put(Mydatabase.TRACKER_NAME, et_name.getText().toString().trim());
            values.put(Mydatabase.IMAGE_NAME, image);
            Mydatabase datatbase = new Mydatabase(getContext());

            int rssi = 10;
            System.out.println("rssi val" + rssi);
            values.put(Mydatabase.RSSI, rssi);
            if (tv_select_alert_type.getText().toString().equalsIgnoreCase("Low")) {
                values.put(Mydatabase.VOLUME_TYPE, 1);
            } else if (tv_select_alert_type.getText().toString().equalsIgnoreCase("High")) {
                values.put(Mydatabase.VOLUME_TYPE, 2);
            }
            values.put(Mydatabase.DISTANCE,tv_select_alert.getText().toString());
            if (tv_select_buzzer.getText().toString().equalsIgnoreCase("Low")) {
                values.put(Mydatabase.BUZZER_TRACKER_VOLUME, 1);
            } else if (tv_select_buzzer.getText().toString().equalsIgnoreCase("High")) {
                values.put(Mydatabase.BUZZER_TRACKER_VOLUME, 2);
            }
            if(uri!=null){
                values.put(Mydatabase.TONE_LOCATION, uri+"/"+id);
            }
            else{
                values.put(Mydatabase.TONE_LOCATION, tv_select_tone.getText().toString());
            }
            values.put(Mydatabase.LATI_TUDEE,0.0);
            values.put(Mydatabase.LONGI_TUDEE,0.0);
            long result = datatbase.AddTracker(values);
            if (result == -1) {
                Utils.ShowSnackBar(getActivity(), "Unable to insert data");
            } else {
                for(int i=0;i<MainActivity.devices.size();i++){
                    if(MainActivity.devices.get(i).getAddress().equalsIgnoreCase(address)){
                        if (tv_select_alert_type.getText().toString().equalsIgnoreCase("Low")) {
                            MainActivity.devices.get(i).setVolume(1);
                        } else if (tv_select_alert_type.getText().toString().equalsIgnoreCase("High")) {
                            MainActivity.devices.get(i).setVolume(2);
                        }
                        if (tv_select_buzzer.getText().toString().equalsIgnoreCase("Low")) {
                            MainActivity.devices.get(i).setBuzzer_volume(1);
                        } else if (tv_select_buzzer.getText().toString().equalsIgnoreCase("High")) {
                            MainActivity.devices.get(i).setBuzzer_volume(2);
                        }
                        if(uri!=null){
                            MainActivity.devices.get(i).setTone_location(uri + "/" + id);
                        }
                        else{
                            MainActivity.devices.get(i).setTone_location("default");
                        }
                        MainActivity.devices.get(i).setDistance(tv_select_alert.getText().toString());
                        MainActivity.devices.get(i).setImage(image);
                        MainActivity.devices.get(i).setName(et_name.getText().toString().trim());
                    }
                }

                Utils.ShowSnackBar(getActivity(), "data inserted successfully");
                datatbase.getDataFromDatabase();
            }
            datatbase.close();
        }


    }

    @OnClick(R.id.iv_pic)
    public void PicClick() {
        BottomSheet.Builder sheet = new BottomSheet.Builder(getActivity(), R.style.BottomSheet_StyleDialog);
        sheet.title("Choose Source");
        sheet.sheet(R.menu.bottom_sheet_menu);
        sheet.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case R.id.camera:
                        try {
                            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(i, CAMERA_CODE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.gallery:
                        try {
                            Intent gallery = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                gallery = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            } else {
                                gallery = new Intent();
                                gallery.setType("image/*");
                                gallery.setAction(Intent.ACTION_GET_CONTENT);
                            }
                            startActivityForResult(gallery, GALLERY_CODE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
        sheet.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                if (data != null && data.getData() != null) {
                    InputStream is = null;
                    try {

                        is = getActivity().getContentResolver().openInputStream(data.getData());
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        bitmap=BitmapFactory.decodeStream(is);
                        System.out.println("size of image is"+bitmap.getByteCount());
                        //iv_pic.setImageBitmap(bitmap);
                        Boolean scaleByHeight = Math.abs(options.outHeight - 100) >= Math.abs(options.outWidth - 100);
                        if (options.outHeight * options.outWidth * 2 >= 200 * 200 * 2) {
                            double sampleSize = scaleByHeight
                                    ? options.outHeight / 100
                                    : options.outWidth / 100;
                            options.inSampleSize =
                                    (int) Math.pow(2d, Math.floor(
                                            Math.log(sampleSize) / Math.log(2d)));
                        }
                        options.inJustDecodeBounds = false;
                        is.close();
                        is = getActivity().getContentResolver().openInputStream(data.getData());
                        bitmap = BitmapFactory.decodeStream(is, null, options);
                        is.close();
                        if (bitmap != null) {
                            String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
                            Cursor cursor = getActivity().getContentResolver().query(data.getData(),
                                    projection, null, null, null);
                            int orientation = 0;
                            if (cursor != null && cursor.moveToFirst()) {
                                orientation = cursor.getInt(0);
                                System.out.println("orientation is" + orientation);
                                cursor.close();
                            }
                            if (orientation != 0) {
                                Matrix matrix = new Matrix();
                                matrix.postRotate(orientation);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                        bitmap.getWidth(), bitmap.getHeight(), matrix,
                                        false);
                                System.out.println("size of image after"+bitmap.getByteCount());
                                iv_pic.setImageBitmap(bitmap);
                            } else {
                                System.out.println("size of image after"+bitmap.getByteCount());
                                iv_pic.setImageBitmap(bitmap);
                            }

                        } else {
                            System.out.println("bitmap is null");
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Utils.ShowSnackBar(getActivity(), "Unable to fetch image");
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Utils.ShowSnackBar(getActivity(), "Request cancelled");
            }


        } else if (requestCode == CAMERA_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                if (data != null && data.getExtras() != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    iv_pic.setImageBitmap(bitmap);
                } else {
                    Utils.ShowSnackBar(getActivity(), "Unable to fetch image");
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Utils.ShowSnackBar(getActivity(), "Request cancelled");
            }


        }
    }

    public void ShowRingtones() {
        dialog = new Dialog(getActivity(), R.style.categorydialogtheme);
        ArrayList<HashMap<String, String>> list = Utils.listRingtones(getActivity());
        HashMap<String,String> map=new HashMap<>();
        System.out.println("list size" + list.size());
        if (list.size() > 0) {
            if (!dialog.isShowing()) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View dialoglayout = inflater.inflate(R.layout.layout_list_ringtones, null);
                dialog.setContentView(dialoglayout);
                ListView lv_list = (ListView) dialoglayout.findViewById(R.id.lv_list);
                lv_list.setVisibility(View.VISIBLE);
                TextView tv_message=(TextView)dialoglayout.findViewById(R.id.tv_message);
                tv_message.setVisibility(View.GONE);
                RingtonesAdapter adapter = new RingtonesAdapter(list, getActivity());
                lv_list.setAdapter(adapter);
                lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        dialog.dismiss();
                        HashMap<String, String> map = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                        tv_select_tone.setText(map.get("title"));
                        uri = map.get("uri").toString();
                        id=map.get("id").toString();
                        System.out.println("uri is"+uri);
                    }
                });
                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.BOTTOM;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.getAttributes().windowAnimations = R.style.Animations_SmileWindow;
                window.setAttributes(wlp);
                dialog.show();
            }
        } else {
            Utils.ShowSnackBar(getActivity(),"No ringtone found");
        }

    }

    @OnClick(R.id.tv_select_alert)
    public void SelectAlert() {
        dialog = new Dialog(getActivity(), R.style.categorydialogtheme);
        if (!dialog.isShowing()) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View dialoglayout = inflater.inflate(R.layout.layout_list_ringtones, null);
            dialog.setContentView(dialoglayout);
            String[] alertzonevalues = getResources().getStringArray(R.array.alertzonevalues);
            ListView lv_list = (ListView) dialoglayout.findViewById(R.id.lv_list);
            lv_list.setVisibility(View.VISIBLE);
            TextView tv_message=(TextView)dialoglayout.findViewById(R.id.tv_message);
            tv_message.setVisibility(View.GONE);
            List<String> value = Arrays.asList(alertzonevalues);
            final SelectionAdapter adapter = new SelectionAdapter(value, getActivity());
            lv_list.setAdapter(adapter);
            lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    dialog.dismiss();
                    String val = (String) adapterView.getItemAtPosition(i);
                    tv_select_alert.setText(val);
                }
            });
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = R.style.Animations_SmileWindow;
            window.setAttributes(wlp);
            dialog.show();
        }
    }

    @OnClick(R.id.tv_select_alert_type)
    public void SelectToneVolume() {
        dialog = new Dialog(getActivity(), R.style.categorydialogtheme);
        if (!dialog.isShowing()) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View dialoglayout = inflater.inflate(R.layout.layout_list_ringtones, null);
            dialog.setContentView(dialoglayout);
            String[] alertzonevalues = getResources().getStringArray(R.array.alerttonetype);
            ListView lv_list = (ListView) dialoglayout.findViewById(R.id.lv_list);
            lv_list.setVisibility(View.VISIBLE);
            TextView tv_message=(TextView)dialoglayout.findViewById(R.id.tv_message);
            tv_message.setVisibility(View.GONE);
            List<String> value = Arrays.asList(alertzonevalues);
            SelectionAdapter adapter = new SelectionAdapter(value, getActivity());
            lv_list.setAdapter(adapter);
            lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    dialog.dismiss();
                    String val = (String) adapterView.getItemAtPosition(i);
                    tv_select_alert_type.setText(val);
                }
            });
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = R.style.Animations_SmileWindow;
            window.setAttributes(wlp);
            dialog.show();
        }
    }

    @OnClick(R.id.tv_select_tone)
    public void SelecTone() {
        ShowRingtones();
    }

    @OnClick(R.id.tv_select_buzzer)
    public void SelectBuzzer() {
        dialog = new Dialog(getActivity(), R.style.categorydialogtheme);
        if (!dialog.isShowing()) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View dialoglayout = inflater.inflate(R.layout.layout_list_ringtones, null);
            dialog.setContentView(dialoglayout);
            String[] alertzonevalues = getResources().getStringArray(R.array.alerttonetype);
            ListView lv_list = (ListView) dialoglayout.findViewById(R.id.lv_list);
            lv_list.setVisibility(View.VISIBLE);
            TextView tv_message=(TextView)dialoglayout.findViewById(R.id.tv_message);
            tv_message.setVisibility(View.GONE);
            List<String> value = Arrays.asList(alertzonevalues);
            SelectionAdapter adapter = new SelectionAdapter(value, getActivity());
            lv_list.setAdapter(adapter);
            lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    dialog.dismiss();
                    String val = (String) adapterView.getItemAtPosition(i);
                    tv_select_buzzer.setText(val);
                }
            });
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = R.style.Animations_SmileWindow;
            window.setAttributes(wlp);
            dialog.show();
        }
    }

    public void onEventMainThread(String state) {
        System.out.println("state inside main thread"+state);
        switch (state) {
            case "2":
                try{
                   // Thread.sleep(2000);
                    ProgressDialogg.dismiss();
                    Utils.ShowSnackBar(getActivity(), "Connected successfully");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                tg_on_off.setChecked(true);
                break;
            case "0":
                try{
                   // Thread.sleep(2000);
                    ProgressDialogg.dismiss();
                    Utils.ShowSnackBar(getActivity(), "Disconnected successfully");

                }
                catch (Exception e){
                    e.printStackTrace();
                }
                tg_on_off.setChecked(false);
                break;
        }

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}

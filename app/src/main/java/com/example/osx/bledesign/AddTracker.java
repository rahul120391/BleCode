package com.example.osx.bledesign;

import android.app.Dialog;
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
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by osx on 24/12/15.
 */
public class AddTracker extends Fragment {
    View view = null;

    @Bind(R.id.iv_trackerimage)
    ImageView iv_trackerimage;

    private static final int GALLERY_CODE = 100;
    private static final int CAMERA_CODE = 200;
    @Bind(R.id.et_name)
    EditText et_name;
    Bitmap bitmap;
    @Bind(R.id.btn_replace_pic)
    Button btn_replace_pic;
    ListDeviceAdapter adapter;
    @Bind(R.id.btn_done)
    Button btn_done;
    Bundle bundle;
    String address;
    PopupWindow popupWindow;
    View dialoglayout;
    ListView lv_listdevices;
    TextView tv_alert;
    LinearLayout layout_center;
    ProgressBar progressbar;
    AppCompatTextView tv_looking;
    Dialog dialog;

    List<DeviceData> data=new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_tracker, null);
        getActivity().setTitle(getString(R.string.add_tracker));
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_hotlist).setVisible(false);
        MenuItem item=menu.findItem(R.id.ic_search);
        item.setVisible(true);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ShowDevices();
                return false;
            }
        });
        super.onPrepareOptionsMenu(menu);
        System.out.println("inside on prepare optionsmenu addtracker");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.iv_trackerimage)
    public void ReplacePic() {
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
                        iv_trackerimage.setImageBitmap(bitmap);
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
                                iv_trackerimage.setImageBitmap(bitmap);
                            } else {
                                iv_trackerimage.setImageBitmap(bitmap);
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
                    iv_trackerimage.setImageBitmap(bitmap);
                } else {
                    Utils.ShowSnackBar(getActivity(), "Unable to fetch image");
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Utils.ShowSnackBar(getActivity(), "Request cancelled");
            }


        }
    }

    @OnClick(R.id.btn_done)
    public void AddToSqlite() {
        if(address==null){
            Utils.ShowSnackBar(getActivity(), "Please select device first");
        }
        else if (et_name.getText().toString().length() == 0) {
            Utils.ShowSnackBar(getActivity(), "Please enter tracker name");
        }
        else{
            Bitmap bitmap = ((BitmapDrawable) iv_trackerimage.getDrawable()).getBitmap();
            Bitmap resizebitmap=getResizedBitmap(bitmap, 300);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizebitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] image = stream.toByteArray();
            int sizeis=image.length;
            System.out.println("sizeis"+sizeis);
            ContentValues values=new ContentValues();
            values.put(Mydatabase.MAC_ADDRESS,address);
            values.put(Mydatabase.TRACKER_NAME,et_name.getText().toString().trim());
            values.put(Mydatabase.IMAGE_NAME,image);
            Mydatabase datatbase=new Mydatabase(getContext());
            if(datatbase.CheckIfRecordExists(address)){
                Utils.ShowSnackBar(getActivity(),"Record already exists");
            }
            else{
                int rssi=10;
                System.out.println("rssi val"+rssi);
                values.put(Mydatabase.RSSI, rssi);
                values.put(Mydatabase.MAC_ADDRESS,address);
                values.put(Mydatabase.VOLUME_TYPE,1);
                values.put(Mydatabase.DISTANCE,"0");
                values.put(Mydatabase.BUZZER_TRACKER_VOLUME,1);
                values.put(Mydatabase.TONE_LOCATION,"default");
                values.put(Mydatabase.LATI_TUDEE,0.0);
                values.put(Mydatabase.LONGI_TUDEE,0.0);
                long result=datatbase.AddTracker(values);
                if(result==-1){
                    Utils.ShowSnackBar(getActivity(),"Unable to insert data");
                }
                else{
                    Utils.ShowSnackBar(getActivity(),"data inserted successfully");

                }
                datatbase.close();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
    public void onEventMainThread(List<DeviceData> list) {
         data=list;
    }
    public void ShowDevices() {
        dialog = new Dialog(getActivity(), R.style.categorydialogtheme);
            if (!dialog.isShowing()) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View dialoglayout = inflater.inflate(R.layout.layout_list_ringtones, null);
                dialog.setContentView(dialoglayout);
                ListView lv_list = (ListView) dialoglayout.findViewById(R.id.lv_list);
                TextView tv_message=(TextView)dialoglayout.findViewById(R.id.tv_message);
                if(data.size()==0){
                    lv_list.setVisibility(View.GONE);
                    tv_message.setVisibility(View.VISIBLE);
                }
                else{
                    lv_list.setVisibility(View.VISIBLE);
                    tv_message.setVisibility(View.GONE);
                    adapter = new ListDeviceAdapter(getActivity(),data);
                    lv_list.setAdapter(adapter);
                    lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            et_name.setText(data.get(i).getName());
                            address=data.get(i).getAddress();
                            dialog.dismiss();

                        }
                    });
                }

                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.BOTTOM;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.getAttributes().windowAnimations = R.style.Animations_SmileWindow;
                window.setAttributes(wlp);
                dialog.show();
        }
        else{
            Toast.makeText(getActivity(),"no ringtones available",Toast.LENGTH_SHORT).show();
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



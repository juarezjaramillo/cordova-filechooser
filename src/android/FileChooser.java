package com.megster.cordova;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final String ACTION_PICK = "pick";
    private static final int PICK_FILE_REQUEST = 1;
    CallbackContext callback;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            chooseFile(callbackContext,null);
            return true;
        }else if(action.equals(ACTION_PICK)){
            chooseFile(callbackContext,args.getString(0));
            return true;
        }

        return false;
    }

    public void chooseFile(CallbackContext callbackContext,String type) {

        // type and title should be configurable
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if("audio".equals(type)||"video".equals(type)||"image".equals(type)){
            intent.setType(type.concat("/*"));
        }else {
            intent.setType("*/*");
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();
                int nameIndex = -1;
                int sizeIndex = -1;

                if (uri != null) {
                    Context context=this.cordova.getActivity().getApplicationContext();
                    Log.w(TAG, uri.toString());
                    JSONObject jo = new JSONObject();
                    String mimeType = context.getContentResolver().getType(uri);
                    Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
                    if(returnCursor!=null)
                    {
                        nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();
                    }


                    try {
                        jo.put("url", uri.toString());
                        jo.put("type", mimeType);
                        if(sizeIndex>-1)
                        {
                            jo.put("size", Long.toString(returnCursor.getLong(sizeIndex)));
                            jo.put("filename", returnCursor.getString(nameIndex));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.success(jo.toString());

                } else {

                    callback.error("File uri was null");

                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                // TODO NO_RESULT or error callback?
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);

            } else {

                callback.error(resultCode);
            }
        }
    }
}
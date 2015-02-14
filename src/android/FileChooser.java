package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import android.provider.MediaStore;
import android.util.Base64;
import android.database.Cursor;
import org.json.JSONObject;


public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;
    CallbackContext callback;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            chooseFile(callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(CallbackContext callbackContext) {

        // type and title should be configurable

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

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

                if (uri != null) {
				
                    String filePath=getRealPathFromURI(cordova.getApplicationContext(),uri);
					JSONObject obj = new JSONObject();
					obj.put("path",filePath );
					obj.put("fileData", encodeFileToBase64Binary(filePath));
					callback.success(uri.toString());

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
	public String getRealPathFromURI (Context context ,Uri contentUri)throws Exception
	{
	     String path = null;
	     String[] proj = { MediaStore.MediaColumns.DATA };

	        if("content".equalsIgnoreCase(contentUri.getScheme ()))
	            {
	                Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
	                if (cursor.moveToFirst()) {
	                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
	                    path = cursor.getString(column_index);
	                }
	                cursor.close();
	                return path;
	            }
	            else if("file".equalsIgnoreCase(contentUri.getScheme()))
	            {
	                return contentUri.getPath();
	            }
	            return null;
	}
		
	private String encodeFileToBase64Binary(String fileName) throws IOException {
		File file = new File(fileName);
		byte[] bytes = loadFile(file);
		byte[] encoded = Base64.encode(bytes, Base64.DEFAULT);
		String encodedString = new String(encoded);
		return encodedString;
	}
	private byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);
	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }
	    byte[] bytes = new byte[(int)length];
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }
 
	    is.close();
	    return bytes;
	}
}
package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;
import android.provider.MediaStore;
import android.content.Context;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import android.widget.Toast;
import android.util.Base64;
import android.os.Build;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent = Intent.createChooser(intent, "Choose a file"); 
		this.cordova.getActivity.startActivityForResult(intent, PICK_FILE_REQUEST);
	
	
		
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }	
	
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
			callback.success( data.getData().getPath().toString());
        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {
                    Log.w(TAG, uri.toString());
					try {
					
						JSONObject obj = new JSONObject();
						obj.put("path",uri );
						//obj.put("fileData", encodeFileToBase64Binary(filePath));
						callback.success( obj.toString());
					
						//String filePath=getRealPathFromURI(this.cordova.getActivity().getApplicationContext(),uri);
						//JSONObject obj = new JSONObject();
						//obj.put("path",filePath );
						//obj.put("fileData", encodeFileToBase64Binary(filePath));
						//callback.success( obj.toString());
					} catch (Exception e) {
						callback.error("You can not select this file ");
					}

                } else {
                    callback.error("You can not select this file ");
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);
            } else {
                callback.error(resultCode);
            }
        }else{
		
		}
    }
	/*public String getRealPathFromURI(Context context, Uri contentUri) {
		  Cursor cursor = null;
		  try { 
		    String[] proj = { MediaStore.Images.Media.DATA };
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		  } finally {
		    if (cursor != null) {
		      cursor.close();
		    }
		  }
		}*/
		
		
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

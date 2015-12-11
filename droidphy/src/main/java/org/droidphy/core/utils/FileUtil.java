package org.droidphy.core.utils;

import android.content.Context;
import android.os.Environment;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import com.orhanobut.logger.Logger;
import org.androidannotations.annotations.EBean;
import org.droidphy.core.R;

import java.io.*;

@EBean
public class FileUtil {

    private Context context;

    public FileUtil(Context context) {
        this.context = context;
    }

    public void write(String content, String filename) throws IOException {
        Closer closer = Closer.create();
        try {
            Writer writer = closer.register(new OutputStreamWriter(
                    context.openFileOutput(filename, Context.MODE_PRIVATE),
                    Charsets.UTF_8));
            writer.write(content);
            writer.flush();
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public String read(String filename) throws IOException {
        Closer closer = Closer.create();
        try {
            Reader reader = closer.register(new InputStreamReader(
                    context.openFileInput(filename),
                    Charsets.UTF_8));
            return CharStreams.toString(reader);
        } catch (FileNotFoundException e) {
            return null;
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public File getWritableDir(String filename) {
        File file = null;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            file = new File(
                    Environment.getExternalStorageDirectory() +
                            "/" + context.getResources().getString(R.string.app_name).toLowerCase(),
                    filename);
            if (file.mkdirs()) {
                Logger.d("Create directory on external storage (" + filename + ")");
            }
        }

        if (file == null || !file.exists()) {
            file = new File(context.getCacheDir(), filename);
            Logger.d("Create directory on internal storage (" + filename + ")");
        }
        return file;
    }
}

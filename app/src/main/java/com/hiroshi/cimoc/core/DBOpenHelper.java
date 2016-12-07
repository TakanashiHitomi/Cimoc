package com.hiroshi.cimoc.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;
import com.hiroshi.cimoc.model.TagDao;
import com.hiroshi.cimoc.model.TagRefDao;
import com.hiroshi.cimoc.model.TaskDao;
import com.hiroshi.cimoc.utils.FileUtils;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/12.
 */
public class DBOpenHelper extends DaoMaster.OpenHelper {

    public DBOpenHelper(Context context, String name) {
        super(context, name);
    }

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        initSource(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                SourceDao.createTable(db, false);
            case 2:
                updateHighlight(db);
            case 3:
                TaskDao.createTable(db, false);
                updateDownload(db);
            case 5:
                updateHHAAZZ();
            case 6:
                SourceDao.dropTable(db, false);
                SourceDao.createTable(db, false);
                TagDao.createTable(db, false);
                TagRefDao.createTable(db, false);
                initSource(db);
                renameDownload();
        }
    }

    private void renameDownload() {
        try {
            File[] sourceDirs =
                    FileUtils.listFiles(FileUtils.getPath(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc", "download"));
            for (File sourceDir : sourceDirs) {
                if (sourceDir.isDirectory()) {
                    for (File comicDir : FileUtils.listFiles(sourceDir)) {
                        if (comicDir.isDirectory()) {
                            String filter = FileUtils.filterFilename(comicDir.getName());
                            if (!filter.equals(comicDir.getName())) {
                                String newPath = FileUtils.getPath(sourceDir.getAbsolutePath(), filter);
                                comicDir.renameTo(new File(newPath));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSource(Database db) {
        String[] title = { "看漫画", "动漫之家", "手机汗汗", "CC图库",
                "有妖气", "动漫屋", "Webtoon", "汗汗漫画", "57漫画"};
        int[] type = { SourceManager.SOURCE_IKANMAN, SourceManager.SOURCE_DMZJ, SourceManager.SOURCE_HHAAZZ,
                SourceManager.SOURCE_CCTUKU, SourceManager.SOURCE_U17, SourceManager.SOURCE_DM5,
                SourceManager.SOURCE_WEBTOON, SourceManager.SOURCE_HHSSEE, SourceManager.SOURCE_57MH};
        List<Source> list = new ArrayList<>(title.length);
        for (int i = 0; i != title.length; ++i) {
            list.add(new Source(null, title[i], type[i], true));
        }
        new DaoMaster(db).newSession().getSourceDao().insertInTx(list);
    }

    private void updateHHAAZZ() {
        if (FileUtils.isDirsExist(FileUtils.getPath(Storage.STORAGE_DIR, "download", "汗汗漫画"))
                && !FileUtils.isDirsExist(FileUtils.getPath(Storage.STORAGE_DIR, "download", "手机汗汗"))) {
            FileUtils.rename(FileUtils.getPath(Storage.STORAGE_DIR, "download", "汗汗漫画"), FileUtils.getPath(Storage.STORAGE_DIR, "download", "手机汗汗"));
        }
    }

    private void updateDownload(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"COMIC\" RENAME TO \"COMIC2\"");
        ComicDao.createTable(db, false);
        db.execSQL("INSERT INTO \"COMIC\" (\"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", \"HIGHLIGHT\", \"UPDATE\", \"FINISH\", \"FAVORITE\", \"HISTORY\", \"DOWNLOAD\", \"LAST\", \"PAGE\")" +
                " SELECT \"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", \"HIGHLIGHT\", \"UPDATE\", null, \"FAVORITE\", \"HISTORY\", null, \"LAST\", \"PAGE\" FROM \"COMIC2\"");
        db.execSQL("DROP TABLE \"COMIC2\"");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void updateHighlight(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"COMIC\" RENAME TO \"COMIC2\"");
        ComicDao.createTable(db, false);
        db.execSQL("INSERT INTO \"COMIC\" (\"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", \"UPDATE\", \"HIGHLIGHT\", \"FAVORITE\", \"HISTORY\", \"LAST\", \"PAGE\")" +
                " SELECT \"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", \"UPDATE\", 0, \"FAVORITE\", \"HISTORY\", \"LAST\", \"PAGE\" FROM \"COMIC2\"");
        db.execSQL("DROP TABLE \"COMIC2\"");
        db.execSQL("UPDATE \"COMIC\" SET \"HIGHLIGHT\" = 1, \"FAVORITE\" = " + System.currentTimeMillis() + " WHERE \"FAVORITE\" = " + 0xFFFFFFFFFFFL);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}

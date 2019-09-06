package com.wrrryyyy.www.mgvideoplayer;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aa on 2018/8/4.
 */

public class VideoItem {
    String path;
    String name;
    String createdTime;
    Bitmap thumb ;
    VideoItem(String strPath,String strName,String createdTime ){
        this.path = strPath;
        this.name = strName;
        SimpleDateFormat sf = new SimpleDateFormat("yy年MM月dd日HH时mm分");
        Date d = new Date(Long.valueOf(createdTime)*1000);
        this.createdTime = sf.format(d);
    }

    void creatThumb() {
        if(this.thumb==null){
            this.thumb = ThumbnailUtils.createVideoThumbnail(this.path, MediaStore.Images.Thumbnails.MINI_KIND);
        }
    }
    void releaseThumb() {
        if(this.thumb!=null) {
            this.thumb.recycle();
            this.thumb = null;
        }
    }

}

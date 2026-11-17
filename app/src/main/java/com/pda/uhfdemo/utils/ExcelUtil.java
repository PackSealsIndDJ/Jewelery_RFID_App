package com.pda.uhfdemo.utils;


import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.pda.uhfdemo.entity.TagInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtil {
    public static WritableFont arial14font = null;

    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(Colour.GRAY_25);

            arial12font = new WritableFont(WritableFont.ARIAL, 10);
            arial12format = new WritableCellFormat(arial12font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    public static void initExcel(String path, String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {

            File file = new File(path + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet(fileName, 0);
            sheet.addCell((WritableCell) new Label(0, 0, fileName, arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            sheet.setRowView(0, 340); 
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(ArrayList<ArrayList<String>> objList, String fileName) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName), workbook);
                WritableSheet sheet = writebook.getSheet(0);

                for (int j = 0; j < objList.size(); j++) {
                    ArrayList<String> list = (ArrayList<String>) objList.get(j);
                    for (int i = 0; i < list.size(); i++) {
                        sheet.addCell(new Label(i, j + 1, list.get(i), arial12format));
                        if (list.get(i).length() <= 5) {
                            sheet.setColumnView(i, list.get(i).length() + 8); 
                        } else {
                            sheet.setColumnView(i, list.get(i).length() + 5);
                        }
                    }
                    sheet.setRowView(j + 1, 350); 
                }

                writebook.write();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public static ArrayList<ArrayList<String>> getRecordData(List<TagInfo> infos) {
        ArrayList<ArrayList<String>> recordList = new ArrayList<>();
        for (int i = 0; i < infos.size(); i++) {
            ArrayList<String> beanList = new ArrayList<String>();
            TagInfo info = infos.get(i);
            beanList.add(info.getIndex() + "");
            beanList.add(info.getType());
            beanList.add(info.getEpc() != null ? info.getEpc() : "");
            beanList.add(info.getTid() != null ? info.getTid() : "");
            beanList.add(info.getUserData() != null ? info.getUserData() : "");
            beanList.add(info.getReserved() != null ? info.getReserved() : "");
            beanList.add(info.getCount() + "");
            beanList.add(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(info.getReadTime()));
            recordList.add(beanList);
        }
        return recordList;
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        String dir = sdDir.toString();
        return dir;
    }

    public static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

    public static void writeTagsToUri(Context context, Uri uri, List<TagInfo> tags) throws IOException, WriteException {
        format();
        OutputStream os = context.getContentResolver().openOutputStream(uri);
        if (os == null) {
            throw new IOException("Cannot open output stream for " + uri);
        }
        WritableWorkbook workbook = Workbook.createWorkbook(os);
        try {
            WritableSheet sheet = workbook.createSheet("Tags", 0);
            String[] colNames = new String[]{"Index", "Type", "EPC", "TID", "UserData", "Reserved", "Count", "ReadTime"};
            sheet.addCell(new Label(0, 0, "Tag Export", arial14format));
            for (int col = 0; col < colNames.length; col++) {
                sheet.addCell(new Label(col, 1, colNames[col], arial10format));
            }
            sheet.setRowView(0, 340);
            sheet.setRowView(1, 350);

            if (tags != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                for (int i = 0; i < tags.size(); i++) {
                    TagInfo info = tags.get(i);
                    int row = i + 2;
                    sheet.addCell(new Label(0, row, String.valueOf(info.getIndex()), arial12format));
                    sheet.addCell(new Label(1, row, info.getType() != null ? info.getType() : "", arial12format));
                    sheet.addCell(new Label(2, row, info.getEpc() != null ? info.getEpc() : "", arial12format));
                    sheet.addCell(new Label(3, row, info.getTid() != null ? info.getTid() : "", arial12format));
                    sheet.addCell(new Label(4, row, info.getUserData() != null ? info.getUserData() : "", arial12format));
                    sheet.addCell(new Label(5, row, info.getReserved() != null ? info.getReserved() : "", arial12format));
                    sheet.addCell(new Label(6, row, String.valueOf(info.getCount()), arial12format));
                    String timeStr = info.getReadTime() != null ? dateFormat.format(info.getReadTime()) : "";
                    sheet.addCell(new Label(7, row, timeStr, arial12format));
                    sheet.setRowView(row, 350);
                }
            }
            workbook.write();
        } finally {
            try {
                workbook.close();
            } catch (Exception ignored) {
            }
            os.close();
        }
    }

}






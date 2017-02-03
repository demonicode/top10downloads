package com.example.shikhar.top10downloads;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Shikhar on 01-01-2017.
 */

public class ParseApplications {
    private static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> appplications;

    public ParseApplications() {
        this.appplications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getAppplications() {
        return appplications;
    }
    public boolean parse(String xmldata){
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;
        String textvalue = "";
        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmldata));
            int eventType = xpp.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT){
                String tagname = xpp.getName();
                switch(eventType){
                    case XmlPullParser.START_TAG:
                        Log.d(TAG, "parse: starting tag for "+ tagname);
                        if("entry".equalsIgnoreCase(tagname)){
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textvalue = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "parse: ending tag for "+tagname);
                        if(inEntry){
                            if("entry".equalsIgnoreCase(tagname)){
                                appplications.add(currentRecord);
                                inEntry=false;
                            }
                            else if ("name".equalsIgnoreCase(tagname)){
                                currentRecord.setName(textvalue);
                            }
                            else if ("artist".equalsIgnoreCase(tagname)){
                                currentRecord.setArtist(textvalue);
                            }
                            else if ("image".equalsIgnoreCase(tagname)){
                                currentRecord.setImageurl(textvalue);
                            }
                            else if ("releaseDate".equalsIgnoreCase(tagname)){
                                currentRecord.setReleaseDate(textvalue);
                            }
                            else if ("summary".equalsIgnoreCase(tagname)){
                                currentRecord.setSummary(textvalue);
                            }
                        }
                        break;
                    default:
                            //nothing to do in default
                    }
                eventType = xpp.next();

                }
            }catch(Exception e){
            status = false;
            e.printStackTrace();
        }
        return status;
    }
}

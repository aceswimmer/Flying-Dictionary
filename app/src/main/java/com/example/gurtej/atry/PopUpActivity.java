package com.example.gurtej.atry;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PopUpActivity extends AppCompatActivity {

    private static final String TAG ="MyPopUpActivity" ;
    TextView tvPopUpWord, tvPopUpMeaning;
    ImageView ivSpeaker, ivGlobe, ivStar,ivShare;
    String s, sU, sMeaning;
    TextToSpeech tts;
    int flag;
    int use;
    ArrayList<String >wordsList=new ArrayList<>();
    DatabaseHelper dbHelper;
    SharedPreferences spref;

    @Override
    protected void onStop() {
        spref.edit().putBoolean("pIsOpen",false).apply();
        super.onStop();
    }

    @Override
    protected void onResume() {
        spref.edit().putBoolean("pIsOpen",true).apply();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: show value whether close or open" +spref.getBoolean("pIsOpen",true));
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up);
        dbHelper=new DatabaseHelper(this);
        spref=getSharedPreferences("MyPrefs",MODE_PRIVATE);
        spref.edit().putBoolean("pIsOpen",true);
        tvPopUpWord = (TextView) findViewById(R.id.tvPopUpWord);
        tvPopUpMeaning = (TextView) findViewById(R.id.tvPopUpMeaning);
        ivSpeaker = (ImageView) findViewById(R.id.ivSpeaker);
        ivGlobe = (ImageView) findViewById(R.id.ivGlobe);
        ivStar = (ImageView) findViewById(R.id.ivStar);
        ivShare= (ImageView) findViewById(R.id.ivShare);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        Intent gotIntent = getIntent();
        s = gotIntent.getStringExtra("guru");
        use=gotIntent.getIntExtra("addToHistory",1);
        if(s==null)
        {
            s=gotIntent.getStringExtra(gotIntent.EXTRA_TEXT);
        }
        Log.d("CHECK THIS OUT", "onCreate: "+gotIntent.getStringExtra(gotIntent.EXTRA_TEXT));
        sU = s.substring(0,1).toUpperCase();
        sU = sU + s.substring(1);
        //setTitle(sU); TODO
        s.toLowerCase();

        sMeaning=gotIntent.getStringExtra("dimi");
        if(sMeaning==null)
        {
            sMeaning=getMeaning(s.toLowerCase());
        }
        flag=gotIntent.getIntExtra("check",1);
        if( sMeaning.length()==0)
        {
            sMeaning="No such word exists!";
            tvPopUpMeaning.setTextColor(Color.RED);
            tvPopUpMeaning.setText("No such word exists!");
            tvPopUpWord.setText(sU+": ");
        }
        else
        {
            tvPopUpMeaning.setText(Html.fromHtml(sMeaning.replace("\n","<br />")));
            tvPopUpWord.setText(sU+": ");

        }
        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sbuild=new StringBuilder();

                sbuild.append("Define " +s +" :\n"+Html.fromHtml(sMeaning.replace("\n","<br />")));
                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, sbuild.toString());
                startActivity(Intent.createChooser(i,"Share Via"));

            }
        });


        if(flag==1 && Words.find(Words.TABLE_NAME_FAV,s,dbHelper.getReadableDatabase()))
        {
            Log.d(TAG, "onCreate: ");
            ivStar.setImageResource(R.drawable.star_yellow);
        }
//        if(flag==1 && use==1)
        if(flag==1)
        {
            if(Words.find(Words.TABLE_NAME_HIST,s,dbHelper.getReadableDatabase()))
            {
                Log.d(TAG, "onCreate: heyyyy");
                Words.removeWord(Words.TABLE_NAME_HIST,s,dbHelper.getWritableDatabase());
//                Words.updateTime(s,new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()),Words.TABLE_NAME_HIST,dbHelper.getWritableDatabase());
//                Log.d(TAG, "onCreate: "+Words.getTime(s,Words.TABLE_NAME_HIST,dbHelper.getReadableDatabase()));
            }
//            else
            Words.addWord(s,new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()),Words.TABLE_NAME_HIST,dbHelper.getWritableDatabase());
        }


        ivSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                ivSpeaker.startAnimation(animFadein);

                tts.speak(sU, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        ivGlobe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                ivGlobe.startAnimation(animFadein);

                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, s);
                startActivity(intent);
            }
        });


        ivStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                ivStar.startAnimation(animFadein);

                if(ivStar.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.star_black).getConstantState())) {

                    if(flag==1) {
                        Log.d("POPUP------>", "onClick: Yellow selected");
                        Toast.makeText(PopUpActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();

                        Words.addWord(s,new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()),Words.TABLE_NAME_FAV,dbHelper.getWritableDatabase());

                       ivStar.setImageResource(R.drawable.star_yellow);
                    }
                    else
                        Toast.makeText(PopUpActivity.this,"Invalid Word",Toast.LENGTH_SHORT);

                } else {
                    Log.d("POPUP------>", "onClick: Black selected");
                    Toast.makeText(PopUpActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                    ivStar.setImageResource(R.drawable.star_black);
                    Words.removeWord(Words.TABLE_NAME_FAV,s,dbHelper.getWritableDatabase());

                }
            }
        });
        ivStar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                return false;
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Perman.onPermResult(requestCode,permissions,grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    String getMeaning(String query) {

        Log.d("SA", "getMeaning: GETMEANING CALLED2");

        String s1 = "<font color = '#606062'>";
        String s2 = "</font>";
        String s3 = "<b><font color = '#0097a7'>";
        String s4 = "</font></b>";

        File file=new File(LogoActivity.DB_PATH);
        if(file.exists() && !file.isDirectory())
        {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(LogoActivity.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            Cursor cursor1 = db.rawQuery("SELECT \n" +
                    "    a.lemma AS 'word',\n" +
                    "    c.definition,\n" +
                    "    c.pos AS 'part of speech',\n" +
                    "    d.sample AS 'example sentence',\n" +
                    "    (SELECT \n" +
                    "            GROUP_CONCAT(a1.lemma)\n" +
                    "        FROM\n" +
                    "            words a1\n" +
                    "                INNER JOIN\n" +
                    "            senses b1 ON a1.wordid = b1.wordid\n" +
                    "        WHERE\n" +
                    "            b1.synsetid = b.synsetid\n" +
                    "                AND a1.lemma <> a.lemma\n" +
                    "        GROUP BY b1.synsetid) AS `synonyms`  \n" +
                    "    \n" +
                    "FROM\n" +
                    "    words a\n" +
                    "        INNER JOIN\n" +
                    "    senses b ON a.wordid = b.wordid\n" +
                    "        INNER JOIN\n" +
                    "    synsets c ON b.synsetid = c.synsetid\n" +
                    "        INNER JOIN\n" +
                    "    samples d ON b.synsetid = d.synsetid\n" +
                    "WHERE\n" +
                    "    a.lemma = ?   \n" +
                    "ORDER BY a.lemma , c.definition , d.sample;", new String[]{query});

            if (!(cursor1.moveToFirst()) || cursor1.getCount() ==0) {
                //cursor is empty
                Cursor cursor2 = db.rawQuery("SELECT \n" +
                        "    a.lemma AS 'word',\n" +
                        "    c.definition,\n" +
                        "    c.pos AS 'part of speech',\n" +
                        "    (SELECT \n" +
                        "            GROUP_CONCAT(a1.lemma)\n" +
                        "        FROM\n" +
                        "            words a1\n" +
                        "                INNER JOIN\n" +
                        "            senses b1 ON a1.wordid = b1.wordid\n" +
                        "        WHERE\n" +
                        "            b1.synsetid = b.synsetid\n" +
                        "                AND a1.lemma <> a.lemma\n" +
                        "        GROUP BY b1.synsetid) AS `synonyms`  \n" +
                        "    \n" +
                        "FROM\n" +
                        "    words a\n" +
                        "        INNER JOIN\n" +
                        "    senses b ON a.wordid = b.wordid\n" +
                        "        INNER JOIN\n" +
                        "    synsets c ON b.synsetid = c.synsetid\n" +
                        "WHERE\n" +
                        "    a.lemma = ? \n" +
                        "ORDER BY a.lemma , c.definition ;", new String[]{query});

                if (!(cursor2.moveToFirst()) || cursor2.getCount() ==0) {
                    //cursor is empty
                    Log.d("NW", "onClick: NOT WORKINGGGGG");
                } else {

                    boolean f = false;
                    int count = 0;

                    StringBuilder definition = new StringBuilder();
                    StringBuilder pos = new StringBuilder();
                    StringBuilder synonyms = new StringBuilder();

                    StringBuilder resList = new StringBuilder();

                    cursor2.moveToFirst();
                    int cnt = cursor2.getCount();

                    String w="", d="", p="", s=null;

                    while(cnt>0) {
                        cnt--;

                        w = cursor2.getString(0);
                        d = cursor2.getString(1);
                        p = cursor2.getString(2);
                        s = cursor2.getString(3);

                        if (!d.equals(definition.toString()) && f) {
                            Log.d("definition", "onClick: "+d);
                            //print previous contents
                            if (s == null || s == "" || s=="null") {
                                resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + "\n");
                                Log.d("definition ", "onClick: OKKKK");
                            } else {
                                resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + s1+"\n\nSynonyms:\n"+s2 + synonyms.toString() + "\n");
                            }
                            //refresh new ones
                            count++;
                            definition = new StringBuilder();
                            definition.append(d);

                            pos = new StringBuilder();
                            pos.append(getPos(p));

                            synonyms = new StringBuilder();

                            if (s!="" && s!=null && !synonyms.toString().equals(s)) {
                                synonyms.append(s);
                            }

                        } else {
                            //append data

                            if (s!="" && s!=null && !synonyms.toString().equals(s)) {
                                synonyms.append(s);
                            }
                            if(!f) {
                                definition.append(d);
                                count++;
                            }
                        }

                        f = true;

                        cursor2.moveToNext();
                    } //while (cursor.moveToNext());

                    if (s == null || s == "" || s=="null") {
                        resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + "\n");
                        Log.d("definition ", "onClick: OKKKK");
                    } else {
                        resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + s1+"\n\nSynonyms:\n"+s2 + synonyms.toString() + "\n");
                    }


                    cursor2.close();
                    db.close();

                    return (resList.toString());

                }

            } else {

                boolean f = false;
                int count = 0;

                StringBuilder definition = new StringBuilder();
                StringBuilder pos = new StringBuilder();
                StringBuilder example = new StringBuilder();
                StringBuilder synonyms = new StringBuilder();

                StringBuilder resList = new StringBuilder();

                cursor1.moveToFirst();
                int cnt = cursor1.getCount();

                String w="", d="", p="", e="", s=null;


                while(cnt>0) {
                    cnt--;

                    w = cursor1.getString(0);
                    d = cursor1.getString(1);
                    p = cursor1.getString(2);
                    e = cursor1.getString(3);
                    s = cursor1.getString(4);

                    if (!d.equals(definition.toString()) && f) {
                        Log.d("definition", "onClick: "+d);
                        //print previous contents
                        if (s == null || s == "" || s=="null" || synonyms.toString() == null) {
                            resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + s1+"\n\nExamples:\n"+s2 + example.toString() + "\n");
                            if (s!=null)
                                Log.d("definition ", "onClick: OKKKK"+s+".");
                        } else {
                            resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + s1+"\n\nExamples:\n"+s2 + example.toString() + s1+"\nSynonyms:\n"+s2 + synonyms.toString() + "\n");
                            Log.d("SA", "getMeaning: SYNONYM EXISTS");
                        }
                        //refresh new ones
                        count++;
                        definition = new StringBuilder();
                        definition.append(d);

                        pos = new StringBuilder();
                        pos.append(getPos(p));

                        example = new StringBuilder();
                        example.append("-"+e+"\n");

                        synonyms = new StringBuilder();

                        if (s!="" && s!=null && !synonyms.toString().equals(s)) {
                            synonyms.append(s);
                        }

                    } else {
                        //append data
                        example.append("-"+e+"\n");

                        if (s!="" && s!=null && !synonyms.toString().equals(s)) {
                            synonyms.append(s);
                        }
                        if(!f) {
                            definition.append(d);
                            count++;
                        }
                    }

                    f = true;

                    cursor1.moveToNext();
                } //while (cursor.moveToNext());
                if (s == null || s == "" || s=="null"|| synonyms.toString() == null) {
                    resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + s1+"\n\nExamples:\n"+s2 + example.toString() + "\n");
                    if (s!=null)
                        Log.d("definition ", "onClick: OKKKK"+s+".");
                } else {
                    resList.append("\n" + s3+count+s4 + ". "+s1+"("+s2 + getPos(p) + s1+") "+s2 + definition.toString() + s1+"\n\nExamples:\n"+s2 + example.toString() + s1+"\nSynonyms:\n"+s2 + synonyms.toString() + "\n");
                    Log.d("SA", "getMeaning: SYNONYM EXISTS");
                }


                cursor1.close();
                db.close();
                return resList.toString();

            }
        }
        else
        {
            Toast.makeText(this, "Database does not exist!", Toast.LENGTH_LONG).show();
        }
        return "";
    }

    String getPos (String pos) {

        String s1 = "<b><font color = '#606062'>";
        String s2 = "</font></b>";

        if (pos.equals("n")) {
            return s1+"noun"+s2;
        } else if (pos.equals("v")) {
            return s1+"verb"+s2;
        } else if (pos.equals("a")) {
            return s1+"adjective"+s2;
        } else if (pos.equals("s")) {
            return s1+"adjective satellite"+s2;
        } else if (pos.equals("r")) {
            return s1+"adverb"+s2;
        }

        return "";
    }

}



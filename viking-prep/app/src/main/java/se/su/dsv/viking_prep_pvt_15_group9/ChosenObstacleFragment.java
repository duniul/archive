package se.su.dsv.viking_prep_pvt_15_group9;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.model.UserTipObject;
import se.su.dsv.viking_prep_pvt_15_group9.util.UserTipListAdapter;


/**
 * Created by Barre on 2015-05-07.
 */
public class ChosenObstacleFragment extends Fragment implements View.OnClickListener {

    private TextView description;
    private TextView trainingtips;
    private TextView usertips;
    private Button descriptionHeader;
    private Button trainingtipsHeader;
    private Button ownTipHeader;
    private Button ownTipViewer;
    private ListView userTipList;
    private SQLiteManager localDatabase;
    private Context ctx;
    private String pickedObstacle;
    private View rootView;
    private UserTipListAdapter userTipListAdapter;
    private ProgressDialog progressDialog;
    private List<UserTipObject> tipList = new ArrayList<UserTipObject>();


    public static ChosenObstacleFragment newInstanceOf() {
        ChosenObstacleFragment fragment = new ChosenObstacleFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_chosen_obstacle, container, false);

// Lista för Tips-objekt

// Tillfälligt skapade testobjkt för tips
//        tipList.add(new UserTipObject("Morgan Alling",0, 0, "Balance", "Hoppa barfota 3 gånger, ", "http://files.parsetfss.com/fdf57403-2863-4d6b-8b53-49ca28b344c6/tfss-380f975b-10f5-43a8-b175-3c62b0c72c75-toughviking.jpeg", "http://files.parsetfss.com/fdf57403-2863-4d6b-8b53-49ca28b344c6/tfss-13a5ee6a-781b-453b-ac9e-0880419e2d3a-toughviking.jpeg"));
//        tipList.add(new UserTipObject("Jonas Karlsson", 0, 0, "Balance", "Spring lite bara", "http://files.parsetfss.com/fdf57403-2863-4d6b-8b53-49ca28b344c6/tfss-380f975b-10f5-43a8-b175-3c62b0c72c75-toughviking.jpeg", "http://files.parsetfss.com/fdf57403-2863-4d6b-8b53-49ca28b344c6/tfss-13a5ee6a-781b-453b-ac9e-0880419e2d3a-toughviking.jpeg"));
//        tipList.add(new UserTipObject("Greger Krok", 0, 0, "Balance", "Ät paprika", "http://files.parsetfss.com/fdf57403-2863-4d6b-8b53-49ca28b344c6/tfss-380f975b-10f5-43a8-b175-3c62b0c72c75-toughviking.jpeg", "http://files.parsetfss.com/fdf57403-2863-4d6b-8b53-49ca28b344c6/tfss-13a5ee6a-781b-453b-ac9e-0880419e2d3a-toughviking.jpeg"));

// Sätter en adapter som hanterar de enskilda tips-objekten
        userTipList = (ListView) rootView.findViewById(R.id.userTip_list);
        userTipListAdapter = new UserTipListAdapter(getActivity(), R.layout.list_row_user_tip, tipList);
        userTipList.setAdapter(userTipListAdapter);
 //       userTipList.setAdapter(new UserTipListAdapter(ctx, R.layout.user_tip_list_item, tipList));
       // tipList.add(new UserTipObject(2, 1, "Balance", "hejehej", " ", " "));


//onToughListener för att se till att ScrollView och ListView kan fungera ihop
        userTipList.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });


// Hämta in värdet för chosenObstacle som skickats med när man väljer hinder

        Bundle bun = new Bundle();
        bun = getArguments();
        pickedObstacle = bun.getString("chosenObstacle");

        TextView textViewHeader = (TextView) rootView.findViewById(R.id.chosenObstacleHead);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

// Sätter knappars innehåll till att vara osynliga innan de togglas

        description = (TextView) rootView.findViewById(R.id.description);
        trainingtips = (TextView) rootView.findViewById(R.id.trainingtips);
        usertips = (TextView) rootView.findViewById(R.id.ownTipHeader);

        descriptionHeader = (Button) rootView.findViewById(R.id.descriptionHeader);
        trainingtipsHeader = (Button) rootView.findViewById(R.id.trainingtipsHeader);
        ownTipHeader = (Button) rootView.findViewById(R.id.ownTipHeader);
        ownTipViewer = (Button) rootView.findViewById(R.id.ownTipViewer);

        descriptionHeader.setOnClickListener(this);
        trainingtipsHeader.setOnClickListener(this);
        ownTipViewer.setOnClickListener(this);
        ownTipHeader.setOnClickListener(this);

        description.setVisibility(View.GONE);
        trainingtips.setVisibility(View.GONE);
        userTipList.setVisibility(View.GONE);


        ImageView obstacleImg = (ImageView) rootView.findViewById(R.id.chosenObstacleImg);


// Kontrollerar värdet för det hinder som valts och bestämmer vilket innehåll som ska visas

        switch (pickedObstacle) {
            case "Balance":
                obstacleImg.setImageResource(getResources().getIdentifier("balancegenomskinlig", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Hindret går ut på att testa deltagarnas balans genom att låta dem gå balansgång på en 30 meter lång planka " +
                        "utan att ramla. Det kanske låter enkelt, men här har många straff-armhävningar kammats in genom åren!");
                trainingtips.setText("För att förbereda sig för detta hinder rekommenderar vi att man tar varje tillfälle i akt för att öva upp " +
                        "sin balans. Gå inte på trottoarerna -gå istället på trottoarkanten! Stå inte bara i bussen, stå istället framlutad på ett ben. " +
                        "Har du utrymme för det och en planka till övers, så rekommenderar vi även att du monterar upp den och börjar balansera!");
                displayObstacles(pickedObstacle);
                break;
            case "Carry-A-Log":
                obstacleImg.setImageResource(getResources().getIdentifier("carryalog", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Vid detta hinder är uppgiften att varje deltagare ska bära en både tung och otymplig vedstock i " +
                        "sina armar under en sträcka på 30 meter, detta utan att tappa den!");
                trainingtips.setText("Här krävs en god hållning och rejäl styrka i överkropp. Vi rekommenderar att du övar upp dig " +
                        "genom att bära tyngsta familjemedlemmen eller vännen varje gång denne får för sig att gå någonstans.");
                displayObstacles(pickedObstacle);
                break;
            case "Ice Tank":
                obstacleImg.setImageResource(getResources().getIdentifier("icetankgenomskinliga", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Detta är hindret för den som inte räds ett kallt dopp! Uppgiften går ut på " +
                        "att ta sig igenom en bassäng fylld med inte bara vatten, utan även is! " +
                        "Exakt hur man tar sig igenom är frivilligt så länge man befinner sig inom bassängens radie, " +
                        "men då bassängen för många deltagare kan upplevas som relativt djup kan ni få räkna med en uppfriskande simtur!");
                trainingtips.setText("För att klara detta hinder krävs det givetvis att man är simkunnig och " +
                        "man bör även ha en relativt hög resistens mot kyla. Vi rekommenderar frekventa besök " +
                        "till närmsta utomhuspool/sjö (gärna under vintertid). För den lyckliga ägaren av ett badkar " +
                        "rekommenderar vi även att man spicar upp det hela ytterligare genom att ersätta bubbel med is. Enjoy!");
                displayObstacles(pickedObstacle);
                break;
            case "Monkeybar":
                obstacleImg.setImageResource(getResources().getIdentifier("monkeybargenomskinlig", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Hindret består av en ställning med 30 stycken stänger på ovansidan. Det är varje deltagares uppgift att med " +
                        "händerna lyckas svinga sig fram stång tills dess att samtliga 30 stänger är avverkade.");
                trainingtips.setText("Här ligger fokus både på grepp- och armstyrka, men även kringliggande muskulatur " +
                        "(så som rygg och axlar) är viktig för arr klara av hindret. Vi rekommenderar dels en varierad " +
                        "träning av överkropp men med lite extra fokus armmuskulatur och greppstyrka. Låt shins och " +
                        "armhävningar bli ert nya beroende, och glöm inte att besöka den lokala lekparken för att svinga er " +
                        "fram i klätterställningarna!");
                displayObstacles(pickedObstacle);
                break;
            case "Pipes":
                obstacleImg.setImageResource(getResources().getIdentifier("pipesgenomskinlig", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Detta hinder är ingenting för den med klaustrofobi! Det består av 30 meter långa rör som deltagarna " +
                        "ska lyckas kräla igenom i snabb takt.");
                trainingtips.setText("Detta hinder testar deltagarens förmåga att snabbt ta sig fram på en begränsad yta, vilket är " +
                        "påfrestande för muskler i hela kroppen. Vi rekommenderar varierad styrketräning samt återkommande krälande i hemmet.");
                displayObstacles(pickedObstacle);
                break;
            case "Reebok 10000 volt":
                obstacleImg.setImageResource(getResources().getIdentifier("reebokvoltgenomskinliga", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Detta är elhindret, det kanske mest fruktade och mest mytomspunna hindret i Tough Viking. " +
                        "Hindret går ut på att varje deltagare ska lyckas ta sig igenom 20 meter av tätt hängande strömledande " +
                        "trådar, på en bana försedd med oregelbundet utplacerade höbalar som hindrar deltagaren från att springa en " +
                        "rak marsch. För att förhöja upplevelsen ytterligare ser vi även till att deltagaren som springer igenom detta " +
                        "hinder även får sig en rejäl dusch med vatten av en av våra högtrycksslangar!");
                trainingtips.setText("Detta hinder är ingenting man kan förbereda sig på enbart genom fysisk träning -här krävs en " +
                        "stor mental förberedelse samt en rejäl nypa viljestyrka. Då det även är fördelaktigt för en deltagare att i " +
                        "förväg vara väl bekant med känslan av att få en stöt så rekommenderar vi återkommande besök till närmsta bondgård " +
                        "för att krama elstängsel!");
                displayObstacles(pickedObstacle);
                break;
            case "Ropes":
                obstacleImg.setImageResource(getResources().getIdentifier("ropesgenomskinlig", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Hindret går ut på att deltagaren ska lyckas klättra upp för en ställning med hjälp av ett 10 meter långt " +
                        "rep. Väl uppe så måste man ju även ta sig ner på andra sidan, även här med hjälp av ett rep.");
                trainingtips.setText("Här krävs det arm- och greppstyrka, men även mycket teknik är involverad. Vi rekommenderar ett " +
                        "uppmonterande av rep på lämpligt ställe för att öva på repklättring.");
                displayObstacles(pickedObstacle);
                break;
            case "Tractor tires":
                obstacleImg.setImageResource(getResources().getIdentifier("traktorgenomskinlig", "drawable", "se.su.dsv.viking_prep_pvt_15_group9"));
                textViewHeader.setText(pickedObstacle);
                description.setText("Hindret består av ett hundratal traktordäck som ligger placerat tätt ihop på ett öppet fält. " +
                        "Det är nu upp till varje deltagare att lyckas ta sig igenom traktorfältet i rask takt utan att ramla!");
                trainingtips.setText("För att klara av detta hunder krävs både en god balans, benstyrka och förmågan att förbestämma " +
                        "sina kommande steg. Vi rekommenderar att man förbereder sig genom terränglöpning och övningar som inkluderar hopp med höga benlyft.");
                displayObstacles(pickedObstacle);

                break;
        }

        return rootView;
    }

// OnKlick för att skapa nytt hindertips

    public void onClick(View v) {
        if (v.getId() == R.id.ownTipHeader) {

            CreateTipFragment fragment = new CreateTipFragment();
            Bundle b = new Bundle();
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", pickedObstacle);
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();


        } else if (v.getId() == R.id.descriptionHeader) {
            description.setVisibility(description.isShown()
                    ? View.GONE
                    : View.VISIBLE);

        } else if (v.getId() == R.id.trainingtipsHeader) {
            trainingtips.setVisibility(trainingtips.isShown()
                    ? View.GONE
                    : View.VISIBLE);

        } else if (v.getId() == R.id.ownTipViewer) {
            userTipList.setVisibility(userTipList.isShown()
                    ? View.GONE
                    : View.VISIBLE);
        }


    }

    private void displayObstacles(final String pickedObstacleName) {
        progressDialog.setMessage("Söker bland användare...");
        progressDialog.show();


        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("ChosenObstacleFragment", "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastSearchError = Toast.makeText(context, errorMsg, duration);
                        toastSearchError.show();

                    } else {
                        JSONObject tips = jsonObject.getJSONObject("tips");
                        int numberOfTips = jsonObject.getInt("number_of_tips");

                        for (int i = 1; i <= numberOfTips; i++) {
                            JSONObject tip = tips.getJSONObject(Integer.toString(i));
                            int tipID = tip.getInt("tip_id");
                            int userID = tip.getInt("user_id");
                            String obstacle = tip.getString("obstacle");
                            String tipText = tip.getString("tip_text");
                            String picture1Url = tip.getString("picture1_url");
                            String picture2Url = tip.getString("picture2_url");
                            String firstName = tip.getString("name");
                            String lastName = tip.getString("surname");

                            String userName = firstName + " " +lastName;


                            tipList.add(new UserTipObject(userName, tipID, userID, obstacle, tipText, picture1Url, picture2Url));
                        }

                        userTipListAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getActivity();
                String errorMsg = error.getMessage();
                int duration = Toast.LENGTH_LONG;

                Log.e("SearchPeopleFragment", "Error: " + errorMsg);

                Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                toastLoginError.show();
                progressDialog.dismiss();
            }
        };

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DB_API_URL, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("tag", "get_tips_by_obstacle");
                params.put("obstacle", pickedObstacleName);
                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_get_tips_by_obstacle");


    }
}

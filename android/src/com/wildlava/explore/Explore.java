//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.view.WindowManager;
import android.widget.TextView.OnEditorActionListener;
import android.graphics.Typeface;
import java.io.InputStreamReader;
//import java.io.FileReader;
import java.io.BufferedReader;

public class Explore extends Activity
{
   private ExpIO io = null;
   private String advname = null;
   private World world = null;
   private TextView output_area;
   private EditText input_area;
   private InputMethodManager imm;
   private Button cave_button;
   private Button mine_button;
   private Button castle_button;
   private Button haunt_button;
   private LinearLayout button_layout;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      LinearLayout layout = new LinearLayout(this);
      output_area = new TextView(this);
      input_area = new EditText(this);

      cave_button = new Button(this);
      mine_button = new Button(this);
      castle_button = new Button(this);
      haunt_button = new Button(this);

      layout.setBackgroundColor(0xff222222);
      output_area.setBackgroundColor(0xff222222);
      output_area.setTextColor(0xff66ff66);
      //output_area.setWidth(320);
      //output_area.setHeight(320);
      //output_area.setHeight(480);
      //output_area.setLines(18);
      //output_area.setLines(64);
      //output_area.setGravity(0x50 | 0x80);
      //output_area.setGravity(Gravity.BOTTOM | Gravity.CLIP_VERTICAL | Gravity.FILL);
      output_area.setGravity(Gravity.BOTTOM | Gravity.CLIP_VERTICAL);
      //output_area.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
      output_area.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1));

      //output_area.setGravity(Gravity.BOTTOM);
      //output_area.setTypeface(Typeface.MONOSPACE);
      output_area.setTextSize((float) 14.0);
      //output_area.setTextSize((float) 9.0);

      input_area.setBackgroundColor(0xff333333);
      input_area.setTextColor(0xffcccccc);
      //input_area.setLines(1);
      input_area.setSingleLine(true);
      input_area.setImeOptions(EditorInfo.IME_ACTION_GO);
      input_area.setWidth(150);
      input_area.setVisibility(View.GONE);

      imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

      input_area.setOnEditorActionListener(new OnEditorActionListener()
      {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
         {
            if (actionId == EditorInfo.IME_ACTION_GO)
            {
               String wish = input_area.getText().toString();
               io.print(wish);
               play(wish);
               input_area.setText("");
            }

            return true;
         }
      });

      button_layout = new LinearLayout(this);

      cave_button.setText("Play Enchanted Cave");
      cave_button.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            start("cave");
         }
      });

      mine_button.setText("Play Lost Mine");
      mine_button.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            start("mine");
         }
      });

      castle_button.setText("Play Medieval Castle");
      castle_button.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            start("castle");
         }
      });

      haunt_button.setText("Play Haunted House");
      haunt_button.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            start("haunt");
         }
      });

      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

      FrameLayout control_layout = new FrameLayout(this);

      layout.setOrientation(LinearLayout.VERTICAL);

      control_layout.setMeasureAllChildren(false);
      //button_layout.setOrientation(LinearLayout.HORIZONTAL);
      button_layout.setOrientation(LinearLayout.VERTICAL);
      button_layout.addView(cave_button);
      button_layout.addView(mine_button);
      button_layout.addView(castle_button);
      button_layout.addView(haunt_button);
      control_layout.addView(input_area);
      control_layout.addView(button_layout);

      //output_area.setBackgroundColor(0xffff0000);
      //control_layout.setBackgroundColor(0xff0000ff);

      layout.addView(output_area);
      layout.addView(control_layout);

      //output_area.setWidth(output_area.getWidth());
      //output_area.setHeight(output_area.getHeight());

      //setContentView(R.layout.main);
      io = new ExpIO(this, output_area, input_area);

      if (savedInstanceState != null)
      {
         advname = savedInstanceState.getString("AdvName");
         if (advname != null)
         {
            // Load the game silently and recover state
            start(advname, true);
            if (world.state(savedInstanceState.getString("SuspendedState")))
            {
               io.setScreen(savedInstanceState.getString("Screen"));
            }
            else
            {
               // If state recovery fails, start over from the beginning
               // (note that this should never happen unless there is a bug
               // in suspend/resume). The user will see a warning about this.
               io.print("");

               world = null;
            }
         }
      }

      if (world == null)
      {
         //input_area.setVisibility(View.GONE);
         //input_area.setFocusable(false);
         //input_area.setEnabled(false);
         //button_layout.setVisibility(View.VISIBLE);

         io.print("Please select an adventure to start.");
         io.print("");
         io.print("If you are new to Explore and would like some tips, enter the \"help\" command after starting a game.");
      }

      setContentView(layout);
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState)
   {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
      if (world != null)
      {
         savedInstanceState.putString("AdvName", advname);
         savedInstanceState.putString("SuspendedState", world.state());
         savedInstanceState.putString("Screen", io.getScreen());
      }

      super.onSaveInstanceState(savedInstanceState);
   }

   public void start(String name)
   {
      start(name, false);
   }

   public void start(String name, boolean silent_load)
   {
      button_layout.setVisibility(View.GONE);
      input_area.setVisibility(View.VISIBLE);
      if (input_area.requestFocus())
      {
         imm.showSoftInput(input_area, InputMethodManager.SHOW_IMPLICIT);
      }

      if (!silent_load)
      {
         io.clearScreen();

         io.print("");
         io.print("");
         io.print("*** EXPLORE ***  ver 4.10");
      }

      advname = name;
      world = new World(io, advname);

      try
      {
         BufferedReader file = null;

         if (advname.equals("cave"))
         {
            file = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.cave)));
         }
         else if (advname.equals("mine"))
         {
            file = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.mine)));
         }
         else if (advname.equals("castle"))
         {
            file = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.castle)));
         }
         else if (advname.equals("haunt"))
         {
            file = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.haunt)));
         }

         if (!silent_load)
         {
            io.print("");
            io.print(advname + " is now being built...");
         }

         try
         {
            if (world.load(file))
            {
               file.close();

               if (!silent_load)
               {
                  io.print("");
                  io.print("");
                  io.print(world.title);
                  io.print("");

                  play(null);
               }
            }
            else
            {
               file.close();

               if (!silent_load)
               {
                  io.print("Error while building adventure!");
               }
            }
         }
         catch (java.io.IOException x)
         {
            if (!silent_load)
            {
               io.print("Error while building adventure!");
            }
         }
      }
      catch (android.content.res.Resources.NotFoundException x)
      {
         if (!silent_load)
         {
            io.print("Sorry, that adventure is not available.");
         }
      }
   }

   void play(String wish)
   {
      int result;

      if (wish != null)
      {
         wish = ExpUtil.superTrim(wish);

         if (!wish.equals(""))
         {
            result = world.processCommand(wish, true);
         }
         else
         {
            result = World.RESULT_NO_CHECK;
         }
      }
      else
      {
         result = World.RESULT_DESCRIBE;
      }

      if ((result & World.RESULT_END_GAME) == 0)
      {
         if ((result & World.RESULT_NO_CHECK) == 0)
         {
            result |= world.checkForAuto(result);
         }
      }

      if ((result & World.RESULT_END_GAME) == 0)
      {
         if ((result & World.RESULT_DESCRIBE) != 0)
         {
            io.print("");
            io.print(world.player.current_room.description());
         }

         io.printRaw(":", false);
      }
      else
      {
         // Hide input area, close keyboard and bring play buttons back
         imm.showSoftInput(input_area, InputMethodManager.HIDE_IMPLICIT_ONLY);
         imm.hideSoftInputFromWindow(input_area.getWindowToken(), 0);
         input_area.setVisibility(View.GONE);
         button_layout.setVisibility(View.VISIBLE);

         if ((result & World.RESULT_WIN) != 0)
         {
            io.print("");
            io.print("Nice job! You successfully completed this adventure!");
         }
         else if ((result & World.RESULT_DIE) != 0)
         {
            io.print("");
            io.print("Game over.");
         }

         io.print("");

         world = null;
      }
   }
}

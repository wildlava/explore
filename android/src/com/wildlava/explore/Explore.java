//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import android.app.Activity;
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
import android.widget.TextView.OnEditorActionListener;
import android.graphics.Typeface;
import java.io.InputStreamReader;
//import java.io.FileReader;
import java.io.BufferedReader;
import android.content.res.Resources.NotFoundException;

public class Explore extends Activity
{
   private ExpIO io = null;
   private String advname = null;
   private World world = null;
   private TextView output_area;
   private EditText input_area;
   private Button cave_button;
   private Button mine_button;
   private Button castle_button;
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
      output_area.setTextSize((float) 12.0);
      //output_area.setTextSize((float) 9.0);

      //input_area.setLines(1);
      input_area.setSingleLine(true);
      input_area.setImeOptions(EditorInfo.IME_ACTION_DONE);
      input_area.setWidth(150);
      input_area.setVisibility(View.GONE);

      input_area.setOnEditorActionListener(new OnEditorActionListener()
      {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
         {
            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
               String wish = input_area.getText().toString();
               io.print(wish);
               play(wish);
               input_area.setText("");

               if (getResources().getConfiguration().orientation ==
                   Configuration.ORIENTATION_LANDSCAPE)
               {
                  // Remove soft keyboard if in landscape mode,
                  // since it covers up the game.
                  return false;
               }
               else
               {
                  // Keep soft keyboard on the screen if in portrait mode,
                  // since it makes for smoother interaction.
                  return true;
               }
            }

            return false;
         }
      });

      button_layout = new LinearLayout(this);

      cave_button.setText("Play Cave");
      cave_button.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            start("cave");
         }
      });

      mine_button.setText("Play Mine");
      mine_button.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            start("mine");
         }
      });

      castle_button.setText("Play Castle");
      castle_button.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
            start("castle");
         }
      });

      if (getResources().getConfiguration().orientation ==
          Configuration.ORIENTATION_PORTRAIT)
      {
         FrameLayout control_layout = new FrameLayout(this);

         layout.setOrientation(LinearLayout.VERTICAL);

         control_layout.setMeasureAllChildren(true);
         button_layout.setOrientation(LinearLayout.HORIZONTAL);
         button_layout.addView(cave_button);
         button_layout.addView(mine_button);
         button_layout.addView(castle_button);
         control_layout.addView(input_area);
         control_layout.addView(button_layout);

         //output_area.setBackgroundColor(0xffff0000);
         //control_layout.setBackgroundColor(0xff0000ff);

         layout.addView(output_area);
         layout.addView(control_layout);

         //output_area.setWidth(output_area.getWidth());
         //output_area.setHeight(output_area.getHeight());
      }
      else
      {
         FrameLayout control_layout = new FrameLayout(this);

         layout.setOrientation(LinearLayout.HORIZONTAL);

         control_layout.setMeasureAllChildren(true);
         button_layout.setOrientation(LinearLayout.VERTICAL);
         button_layout.addView(cave_button);
         button_layout.addView(mine_button);
         button_layout.addView(castle_button);
         control_layout.addView(input_area);
         control_layout.addView(button_layout);

         //output_area.setBackgroundColor(0xffff0000);
         //control_layout.setBackgroundColor(0xff0000ff);

         layout.addView(output_area);
         layout.addView(control_layout);

         //output_area.setWidth(output_area.getWidth());
         //output_area.setHeight(output_layout.getHeight());
      }

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
         input_area.setVisibility(View.GONE);
         //input_area.setFocusable(false);
         //input_area.setEnabled(false);
         //button_layout.setVisibility(View.VISIBLE);

         io.print("Please select an adventure.");
         io.print("");
         io.print("If you want to start over or choose a different");
         io.print("adventure while playing, type \"quit\".");
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
      //input_area.setFocusable(true);
      //input_area.setEnabled(true);
      input_area.setVisibility(View.VISIBLE);
      input_area.requestFocus();

      if (!silent_load)
      {
         io.clearScreen();

         io.print("");
         io.print("");
         io.print("*** EXPLORE ***  ver 4.8.6");
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

         if (!silent_load)
         {
            io.print("");
            io.print(advname + " is now being built...");
         }

         if (world.load(file))
         {
            try
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
            catch (java.io.IOException x)
            {
               if (!silent_load)
               {
                  io.print("Error while building adventure!");
               }
            }
         }
         else
         {
            try
            {
               file.close();
            }
            catch (java.io.IOException x)
            {
            }

            if (!silent_load)
            {
               io.print("Error while building adventure!");
            }
         }
      }
      catch (NotFoundException x)
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
            result = world.RESULT_NORMAL;
         }
      }
      else
      {
         result = world.RESULT_DESCRIBE;
      }

      if ((result & world.RESULT_NO_CHECK) == 0)
      {
         int check_result = world.checkForAuto(result);
         if (check_result != world.RESULT_NORMAL)
         {
            result = check_result;
         }
      }

      if ((result & world.RESULT_DESCRIBE) != 0)
      {
         io.print("");
         io.print(world.player.current_room.description());
      }

      //if ((result & world.RESULT_WIN) != 0 ||
      //    (result & world.RESULT_DIE) != 0)
      if ((result & world.RESULT_END_GAME) != 0)
      {
         // Hide input area, close keyboard and bring play buttons back
         //input_area.setFocusable(false);
         //input_area.setEnabled(false);
         input_area.setVisibility(View.GONE);
         InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(input_area.getWindowToken(), 0);
         button_layout.setVisibility(View.VISIBLE);

         if ((result & world.RESULT_WIN) != 0)
         {
            io.print("");
            io.print("Congratulations, you have successfully completed this adventure!");
         }
         else if ((result & world.RESULT_DIE) != 0)
         {
            io.print("");
            io.print("Game over.");
         }

         io.print("");

         world = null;
      }
      else
      {
         io.printRaw(":", false);
      }
   }
}

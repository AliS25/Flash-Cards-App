package com.example.flashcardsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    FlashcardDatabase flashcardDatabase;
    List<Flashcard> allFlashcards;
    int currentCardDisplayedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashcardDatabase = new FlashcardDatabase(getApplicationContext());
        allFlashcards = flashcardDatabase.getAllCards();


        TextView flashcardQuestion = findViewById(R.id.flashcard_question);
        TextView flashcardAnswer = findViewById(R.id.flashcard_answer);

        if (!allFlashcards.isEmpty()) {
            flashcardQuestion.setText(allFlashcards.get(0).getQuestion());
            flashcardAnswer.setText(allFlashcards.get(0).getAnswer());
        }

        flashcardQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the center for the clipping circle
                int cx = flashcardAnswer.getWidth() / 2;
                int cy = flashcardAnswer.getHeight() / 2;
                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);
                // create the animator for this view (the start radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(flashcardAnswer, cx, cy, 0f, finalRadius);
                // hide the question and show the answer to prepare for playing the animation!
                flashcardQuestion.setVisibility(View.INVISIBLE);
                flashcardAnswer.setVisibility(View.VISIBLE);

                anim.setDuration(3000);
                anim.start();
            }
        });
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddCardActivity.class);
                //     MainActivity.this.startActivity(intent);

                MainActivity.this.startActivityForResult(intent, 100);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allFlashcards==null||allFlashcards.size()==0){
                    return;
                }
                // advance our pointer index so we can show the next card
                currentCardDisplayedIndex++;
                // make sure we don't get an IndexOutOfBoundsError if we are viewing the last indexed card in our list
                if(currentCardDisplayedIndex>=allFlashcards.size()){
                    Snackbar.make(flashcardQuestion, "You've reached the end of the cards, going back to start.",Snackbar.LENGTH_SHORT).show();
                    currentCardDisplayedIndex=0;
                }




                final Animation leftOutAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.left_out);
                final Animation rightInAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.right_in);
                leftOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // this method is called when the animation first starts
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //moved previous logic to here
                        // set the question and answer TextViews with data from the database
                        flashcardQuestion.setText(allFlashcards.get(currentCardDisplayedIndex).getQuestion());
                        flashcardAnswer.setText(allFlashcards.get(currentCardDisplayedIndex).getAnswer());// this method is called when the animation is finished playing

                        findViewById(R.id.flashcard_question).startAnimation(rightInAnim);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // we don't need to worry about this method
                    }
                });
                findViewById(R.id.flashcard_question).startAnimation(leftOutAnim);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100) { // this 100 needs to match the 100 we used when we called startActivityForResult!
                String question = data.getExtras().getString("string1"); // 'string1' needs to match the key we used when we put the string in the Intent
                String answer = data.getExtras().getString("string2");
                TextView flashcardQuestion = findViewById(R.id.flashcard_question);
                TextView flashcardAnswer = findViewById(R.id.flashcard_answer);
                flashcardQuestion.setText(question);
                flashcardAnswer.setText(answer);

                flashcardDatabase.insertCard(new Flashcard(question, answer));
                allFlashcards= flashcardDatabase.getAllCards();
            }


        }
    }
}

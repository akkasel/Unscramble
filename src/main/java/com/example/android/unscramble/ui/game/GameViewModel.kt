package com.example.android.unscramble.ui.game

import android.text.Spannable
import android.text.SpannableString
import android.text.style.TtsSpan
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    private val TAG = "GameFragment"

    // to make '_score' accessible and editable only within GameViewModel
    // so the GameFragment can still read its value using the read-only property('score')
    private val _score = MutableLiveData(0)
    val score : LiveData<Int>
        get() = _score

    private val _currentWordCount = MutableLiveData(0)
    val currentWordCount : LiveData<Int>
        get() = _currentWordCount

    // to make '_currentScrambledWord' accessible and editable only within GameViewModel
    // so the GameFragment can still read its value using the read-only property('currentScrambledWord)
    private val _currentScrambledWord = MutableLiveData<String>()

    // to have Talkback read aloud the individual characters of the scrambled word. Within the GameViewModel,
    // convert the scrambled word String to a Spannable string.
    // A spannable string is a string with some extra information attached to it.
    // In this case, we want to associate the string with a TtsSpan of TYPE_VERBATIM,
    // so that the text-to-speech engine reads aloud the scrambled word verbatim, character by character.
    val currentScrambledWord: LiveData<Spannable> = Transformations.map(_currentScrambledWord) {
        if (it == null) {
            SpannableString("")
        } else {
            val scrambledWord = it.toString()
            val spannable: Spannable = SpannableString(scrambledWord)
            spannable.setSpan(
                TtsSpan.VerbatimBuilder(scrambledWord).build(),
                0,
                scrambledWord.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            spannable
        }
    }

    private var wordList : MutableList<String> = mutableListOf()
    private lateinit var currentWord : String

    init {
        // add new message on the Logcat when debugging, to better understand the ViewModel Lifecycle.
        Log.d(TAG, "GameViewModel Created!")

        // to display a scrambled word at the start of the app.
        getNextWord()
    }

    // to get the next word.
    private fun getNextWord() {

        // to get a random word, and assign it to 'currentWord'
        currentWord = allWordsList.random()

        // convert the currentWord string to an array.
        val tempWord = currentWord.toCharArray()

        // shuffle the word.
        tempWord.shuffle()

        // shuffle the characters on this array using 'shuffle()' method.
        while (tempWord.toString().equals(currentWord, false)) {
            tempWord.shuffle()
        }

        // to check if a word has been used already.
        // if wordlist contains current word, get the next word.
        // else, update the currentWord to newly scrambled word, increase the word count, and add that new word to the wordList.
        if (wordList.contains(currentWord)) {
            getNextWord()
        } else {
            _currentScrambledWord.value = String(tempWord)
            _currentWordCount.value = (_currentWordCount.value)?.inc()
            wordList.add(currentWord)
        }

    }

    // Returns true if the current word count is less than MAX_NO_OF_WORDS.
    // + Updates the next word.
    fun nextWord() : Boolean {
        return if (_currentWordCount.value!! < MAX_NO_OF_WORDS) {
            getNextWord()
            true
        } else false
    }

    // to increase the 'score' variable.
    private fun increaseScore() {
        _score.value = (_score.value)?.plus(SCORE_INCREASE)
    }

    // to validate the player's word and increase the score if the guess is correct.
    // & this will update the final score in your alert dialog.
    fun isUserWordCorrect(playerWord : String) : Boolean {
        if (playerWord.equals(currentWord, true)) {
            increaseScore()
            return true
        }
        return false
    }

    // Re-initialize the game data to restart the game.
    fun reinitializeData() {
        _score.value = 0
        _currentWordCount.value = 0
        wordList.clear()
        getNextWord()
    }

}
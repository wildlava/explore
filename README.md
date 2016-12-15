# Explore - The Adventure Interpreter #


### Running explore ###

Simply type "explore" (it will start the Java program). There are five
main adventures, and if you want to try one of these, type the name of
the adventure (cave, mine, castle, haunt, or porkys) when asked. Here is
some more info on these games:


Cave - "Enchanted Cave" was the first of our adventure games. The fact
that it takes place in a cave, like the original Adventure, was no
coincidence. This adventure had lots of rooms, but the capabilities of the
Explore Adventure Language were just being developed, so even though I think
this one came out pretty well, it's not as rich in features as the later ones.

Mine - "Lost Mine" takes place in an old coal mine in a desert environment,
complete with scary skeletons, mining cars, and lots of magic. We started to
get a little more descriptive in this one, and we also added features to
the adventure language to make things seem a little "smarter."

Castle - "Medieval Castle" was the final in the "trilogy" of our late-nite
teenage adventure creativity. This one forced us to add even more features to
the language, and I believe it really became "sophisticated" with this one.
Castle is perhaps the most colorful of the adventures, but not as mystical
somehow as Enchanted Cave. De and I didn't make any more games after this one.

Haunt - "Haunted House" was not an original creation. It is a clone of
Radio Shack's Haunted House adventure game that I re-created in the
Explore Adventure Language as a test of the language's power. I had to
play the original quite a bit to get it right, since I was going on the
behavior of the game and not its code.

Porkys - "Porky's" is the only one in which I had no involvement. A friend
in Oklahoma at the time took the Explore language and created this one,
inspired by the movie of the same name. It was especially cool to play and
solve an adventure written by someone else with my own adventure language!
Warning, this one has "ADULT CONTENT AND LANGUAGE!"


There is a Python version that is maintained, as well (to run, type
"python2 python/explore.py"). There are several command line options
for this one as well, mostly for running the program inside another script
or program. For example, my web-based version uses these features.
It is located here:

    http://www.wildlava.com/explore/


### History ###

When I was 15 or so, my cousin, De, and I were into playing adventure games,
like the mother of all text adventure games, "Adventure". We wanted to make
our own, so we wrote a simple one, but it was hard-coded and was a pain
to create. So we came up with the idea to make a program that could interpret
adventure "game files" that were written in a kind of adventure "language".
So we both wrote programs in BASIC to do this on TRS-80 computers (wow,
1.77 MHz!), and we wrote adventures in separate text files. We later merged
our work into this program, which was dubbed "Explore". By the way, I was
really bummed when a guy named Scott Adams (not the Dilbert dude!) came out
with a commercial program that used the same concept! Just think of all the
money we could have made!

We came up with three adventures that were written in the wee hours of the
morning on three separate occasions listening to Steely Dan. It was kind of
a mystical inspiration I would say.

De is no longer with us, but these games live on for me as a great memory
of our friendship, and I hope that they allow a little piece of him to endure.

Years later I dug up the old BASIC program and rewrote it in C (note that the
C version and the BASIC version are no longer being maintained, so future
adventure game files or newer revisions of the old ones won't work with the
old code).

A few years after this I rewrote the whole system in Java as a way to learn
the language. And years after that, I rewrote the whole thing in Python and
later in Ruby.


### To do ###

* Are there hard-wrapped strings (resume in python)? Fix?

* Return error if saved_adventure_version > version

* Fix (?) logic of comma-separated OLD VERSION case (does not add to delta)

* Fix checksum code to keep bytes <= 0x3f (to avoid workaround for '`' to ' '
  workaround for "corner case where resume can fail" in 4.8.3 (note that we
  need to keep corner case fix for suspend versions before this fix).

* Include adventure name in suspend string

* Remove logic for now-unsupported suspend_version < 1


### Resolved questions ###

* Old BASIC Explore did a "You can't do that yet." if you try a custom
  command that's also a builtin without the required object. New
  java & python ones fall through to the builtin. Which is more right?
  Crown example (castle) suggests the new way, oil example perhaps not.
  Python, Java, and Ruby versions now have a variable called "trs_compat"
  that controls this.

  - New "fixed object" feature takes care of this.


### General bugs/questions ###

* If you go to a non-existant room, you get "you can't go that way" - OK?

* Should we break on error in takeAction()? We don't break on error if, e.g.,
  = command is used to do a regular command that may fail. Perhaps we should
  let all actions get run (or do later actions rely on successful completion
  of previous ones?).

* If there is a room with name None or nil, it would serve as a destination
  for walking to a blocked or non-existant room.

* If "/" command tries to send you to a non-existant room, no error
  message is returned.

* Walking into breakfast room and dropping knife prints "Ok"
  grouped with action stuff (getting thrown out by knight).
  Old BASIC version prints "Ok" normally with blank line after.

* Multiple duplicate globals should work requiring different objects.

* What should be done for multiple ACTION lines after one COMMAND?

* Variables (TITLE, ROOM, etc.) are not case insensitive.

* Commands (in adventure file) have to be upper case.

* If commands typed too fast, they appear before prompt (Linux issue).


### Java issues ###

* If there is minimal stuff in adventure file, NullPointerException thrown
  (may be fixed now).

* Check string passing or referencing for potential issues
  (e.g. expandItemName returns a reference to a string in the list).


### Java applet issues (no longer used for web-based game) ###

* Is there a limit to the textArea length in explore?
  ...and do lockups when playing a long time and/or fast typing command<cr>
  have to do with this limit or with deadlocking threads?
  Also, it gets really slow when this gets too large!

* Can't cut/paste in applet in order to suspend/resume.


### C issues (no longer maintained) ###

* Rework itemcontainer so it doesn't leave holes in list.

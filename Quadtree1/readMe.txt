Go to the directory with the contained jar file

java -jar QuadTreeManager.jar test.sqlite instructions.txt  

This will create new tables. For testing the required methods, just insert into the instructions text file I created, please make sure to keep the letters in the beginning.
The letter in the beginning is my sensor to what type of method it should invoke.

An example would be the following:

c MyAwesomeTree -5.2 0 10.6 32.7
i MyAwesomeTree SpatiallyInteresting -3.1 7.8
i MyAwesomeTree SpatiallyInteresting -3.1 20
i MyAwesomeTree blah -3.1 21
i MyAwesomeTree other 6 7
i MyAwesomeTree stuff 7 7
l MyAwesomeTree -4 1 0 18 0 5

etc. 


You can also run the program in the project src file folder. Just compile and then 
java QuadTreeManager test.sqlite instructions.txt 

Jar is just so much easier so you don't have to worry about external libraries

The java files are also in the src folder if you need to check java files for cheating or hardcoding

I did this HW alone
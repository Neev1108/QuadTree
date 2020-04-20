Explanation for this HW
A quadtree is a data structure used to store multidimensional data, and categorize the data. Each parent node only has 4 children, which in this case means that each rectangle only has 4 points. IF it has more than 4 points, then it would be split up into 4 new parent nodes, readjusting the x and y of the points into the new rectangles created. The rectangles have to be created by calculating the midpoints of the initial rectangle etc. Instructions need to be put into the instructions.txt file which will translated to the instructions for inputing into the database tables. 

Instructions for this HW 

Your program QuadTreeManager will be run from the command line with a line like:

java QuadTreeManager sqlite_database_name name_of_instruction_file
For example,

java QuadTreeManager test.sqlite instructions.txt
The above command should create a connection to the Sqlite database test.sqlite and then execute each of the instructions in instructions.txt on that database. An instruction file consists of a sequence of lines, each line being an instruction. These can be one of three types:

A create quad tree line which creates a new quad tree. It has the format:
c file_name low_x low_y high_x high_y

This creates a new quad tree named file_name with rectangle given by the points (low_x, low_y) and (high_x, high_y). All points which we insert into this tree need to be within this rectangle to your code should output an error. Similarly, we require low_x to be less than high_x and low_y to be less than high_y. The following is an example of a create instruction:
c MyAwesomeTree -5.2 0 10.6 32.7

An insert quad tree point which inserts a record into a quad tree. It has the format:
i file_name label x_value y_value
This instruction should insert into the quad tree file_name (only if it exists) the record given by label x_value y_value. The following is an example of an insert instruction:
i MyAwesomeTree SpatiallyInteresting -3.1 7.8

This should insert into the quad tree MyAwesomeTree a record with label SpatiallyInteresting at location (-3.1, 7.8).
A lookup quad tree points which looks up the records in the quad tree contained within a rectangle given by two points. It has the format:
l file_name p1_x p1_y p2_x p2_y limit_offset limit_count
This should first output to standard output (usually print to the terminal) the coordinates of the quad tree rectangles which intersect with the the rectangle given by the pair of points (p1_x, p1_y) and (p2_x p2_y). This portion of the output should be in the format:
Query intersects with the following rectangles in the quad tree:
Rect_1:(some_low_x1, some_low_y1), (some_high_x1, some_high_y1)
...
Rect_n:(some_low_xn, some_low_yn), (some_high_xn, some_high_yn)
For example, if we also add to our MyAwesomeTree the points (-3.1, 20) with label "blah", (-2.1, 21) with label "blah2", (-1.1, 22) with label "blah3", (-0.1, 25) with label "blah4" (so had to split the root quad), and then had the line:
l MyAwesomeTree -4 1 0 18 0 5
then this portion of the output would look like:
Query intersects with the following rectangles in the quad tree:
Rect_1:(-5.2, 0), (2.7, 16.35)
Rect_2:(-5.2, 16.35), (2.7, 32.7)
The rest of the output should look like:
Quad tree records satisfying the query:
(label_{limit_offset}, x_{limit_offset}, y_{limit_offset})
...
(label_{limit_offset+limit_count}, x_{limit_offset+limit_count}, y_{limit_offset+limit_count})
I.e., the string "Quad tree records satisfying the query:\n" followed by a sequence of lines listing up to limit_count many records in the quad tree file_name beginning limit_offsetth record found (starting at 0) that belongs to the rectangle given by the pair of points (p1_x, p1_y) and (p2_x p2_y). So for our example above, the rest of the output for this instruction would be:
Quad tree records satisfying the query:
(SpatiallyInteresting, -3.1, 7.8)
To implement the above instructions, the QuadTreeManager.java file should contain a class QuadTreeManager which has the following public methods which roughly correspond to each of these operations:

QuadTreeManager(String databaseName) -- the constructor takes the name of the Sqlite database, the QuadTreeManager is supposed to manage the quad trees for. It then creates the following three tables in this database if they don't exist:
QUAD_TREE(FILE_NAME VARCHAR(64), ROOT_RECT_ID INTEGER)
QUAD_TREE_RECT(ID INTEGER AUTOINCREMENT, PARENT_ID INTEGER, X_LOW REAL, Y_LOW REAL, X_HIGH REAL, Y_HIGH REAL, PRIMARY_KEY(ID))
QUAD_TREE_POINT(QUAD_RECT_ID INTEGER, X REAL, Y REAL, LABEL CHAR(16)))
QUAD_TREE_RECT should probably also have an index on PARENT_ID
boolean createQuadTree(String fileName, float lowX, float lowY, float highX, float highY). This method should insert a row into the table QUAD_TREE_RECT with values -1 for the PARENT_ID and lowX, lowY, highX, highY for the bounding coorindates of the quad tree. Don't specify the ID, but rely on AUTOINCREMENT. The meothod should get the ID of this added row after it is inserted. Then usinng this ID and filename it should insert a row into QUAD_TREE. The return value of this method should indicate if it succeeded to do the insert or not.
boolean add(String fileName, QuadRecord record). This function should insert the passed QuadRecord into the quad tree given by fileName. To do this it should look up in QUAD_TREE to see if such a quad tree exists. If it does, it should get the QUAD_TREE_RECT row corresponding to ROOT_RECT_ID, and see if the record has a point that can be inserted into this quad tree. If it does it should do a query for QUAD_TREE_RECT rows with PARENT_ID having value ROOT_RECT_ID, and so on to trace down through the quad tree to where the record should be inserted. If a quad needs to be split (is a leaf with more than 4 records) the appropriate QUARD_TREE_RECT rows should be added to the table and existing QUAD_TREE_POINT rows in the now subdivided quad should have their QUAD_RECT_ID's updated. Finally, an appropriate new QUAD_TREE_POINT record should be added for record.
QuadRectangle[] lookupRectangle(String fileName, QuadRectangle r) This function should return an array of QuadRectangle's (if they exists) from the quad tree fileName such that each rectangle non-trivially intersects with r such that the rectangle doesn't have child rectangles (QUAD_TREE_RECT's whose PARENT_ID is the ID of the rectangle).
QuadRecord[] lookupPoint(String fileName, QuadPoint pt1, QuadPoint pt2, int limit_offset, int limit_count) This function should return an array of up to limit_count (if they exists) QuadRecord's from the quad tree fileName beginning with the limit_offsetth such record found (starting at 0) that lies in the rectangle given by the points pt1 and pt2.
This completes the description of the program you need to write.






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
